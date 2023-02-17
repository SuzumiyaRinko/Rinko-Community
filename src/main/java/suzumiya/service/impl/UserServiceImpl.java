package suzumiya.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import suzumiya.constant.CacheConst;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.UserRegisterDTO;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.FollowingSelectVO;
import suzumiya.service.IUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService, UserDetailsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource(name = "userCache")
    private Cache<String, Object> userCache; // Caffeine

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    private static final String TOKEN_KEY = "114514"; // Token密钥

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /* 查询用户信息 */
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public String login(User user, HttpServletRequest request) {
        /* 判断该ip是否暂时不允许登录（账号密码连续输错5次） */
        String ip = request.getRemoteHost().replaceAll(":", "-"); // 原ip大概是 0:0:0:0:0:0:0:1
        Object cnt = redisTemplate.opsForValue().get(RedisConst.LOGIN_RETRY_USER_KEY + ip);
        if (cnt != null && (int) cnt % 5 == 0) {
            redisTemplate.expire(RedisConst.LOGIN_RETRY_USER_KEY + ip, 30L, TimeUnit.SECONDS);
            throw new RuntimeException("请在30s后再重新尝试");
        }

        /* 判断注册的账号密码是否符合要求 */
        String username = user.getUsername();
        String password = user.getPassword();
        if (username != null && !(username.matches(CommonConst.REGEX_EMAIL) && password.matches(CommonConst.REGEX_PASSWORD))) {
            throw new RuntimeException("账号或密码不符合要求");
        }

        /* 生成Authentication对象，让SS做校验 */
        // 获取当前用户的salt
        // username为null不会抛异常
        User existedUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        // 判断账号不存在
        if (existedUser == null) {
            throw new RuntimeException("该账号不存在");
        }
        // 判断账号是否已激活
        if (existedUser.getActivation() == 0) {
            throw new RuntimeException("该账号未激活");
        }
        user.setSalt(existedUser.getSalt());
        // 验证账号密码
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword() + user.getSalt());
        Authentication authentication;
        try {
            // SS返回UserDetail
            // 这里会调用UserDetail.getAuthorities()方法，但是登录时authoritiesStr为空，所以此时UserDetail.getAuthorities()返回null
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException ex) {
            redisTemplate.opsForValue().increment(RedisConst.LOGIN_RETRY_USER_KEY + ip, 1L);
            redisTemplate.expire(RedisConst.LOGIN_RETRY_USER_KEY + ip, 30L, TimeUnit.SECONDS);
            throw new RuntimeException("账号或密码错误");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("登录接口异常");
        }

        /* 查询用户权限 */
        User authenticatedUser = (User) authentication.getPrincipal();
        List<String> authoritiesStr = userMapper.getAuthoritiesStrByUserId(authenticatedUser.getId());
        authenticatedUser.setAuthoritiesStr(authoritiesStr);

        /* 把用户信息存放到Redis中，TTL为30mins */
        String key = RedisConst.LOGIN_USER_KEY + authenticatedUser.getId();
        Map<String, Object> value = new HashMap<>();
        BeanUtil.beanToMap(authenticatedUser, value, true, null);
        redisTemplate.opsForHash().putAll(key, value);
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);

        /* 生成并返回Jwt */
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("userId", authenticatedUser.getId());
        return "Bearer " + JWTUtil.createToken(payload, TOKEN_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void logout() {
        /* 删除Redis中的用户信息 */
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redisTemplate.delete(RedisConst.LOGIN_USER_KEY + user.getId());
    }

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        /* 判断该IP 24小时内的注册次数 */
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        String ip = request.getRemoteHost().replaceAll(":", "-"); // 原ip大概是 0:0:0:0:0:0:0:1
        Object t = redisTemplate.opsForValue().get(RedisConst.REGISTER_TIMES_KEY + ip);
        if (t != null && (int) t % 5 == 0) {
            throw new RuntimeException("当天注册次数已到达5次，请在24小时后再尝试注册");
        }

        /* 判断验证码是否正确 */
        if (!StrUtil.equals(userRegisterDTO.getCorrectCode(), userRegisterDTO.getCode())) {
            // 因为前端有检测，不正确说明用户绕过了前端，所以直接抛异常即可
            throw new RuntimeException("验证码不正确");
        }

        /* 判断注册的账号密码是否符合要求 */
        String username = userRegisterDTO.getUsername();
        String password = userRegisterDTO.getPassword();
        if (username != null && !(username.matches(CommonConst.REGEX_EMAIL) && password.matches(CommonConst.REGEX_PASSWORD))) {
            throw new RuntimeException("账号或密码不符合要求");
        }

        /* 判断当前用户名是否已经存在 */
        User existedUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userRegisterDTO.getUsername()));
        if (existedUser != null) {
            throw new RuntimeException("当前用户名已存在");
        }

        /* 新增用户 */
        String salt = UUID.randomUUID().toString().substring(0, 8); // [0, 8)
        String encodePassword = passwordEncoder.encode(userRegisterDTO.getPassword() + salt);

        User newUser = new User();
        newUser.setUsername(userRegisterDTO.getUsername());
        newUser.setPassword(encodePassword);
        newUser.setSalt(salt);

        String uuid = UUID.randomUUID().toString();
        newUser.setActivationUUID(uuid);
        userMapper.insert(newUser);

        /* 记录该IP 24小时内的注册次数 */
        if (Boolean.FALSE.equals(redisTemplate.hasKey(RedisConst.REGISTER_TIMES_KEY + ip))) {
            redisTemplate.opsForValue().increment(RedisConst.REGISTER_TIMES_KEY + ip); // 注册次数+1
            redisTemplate.expire(RedisConst.REGISTER_TIMES_KEY + ip, 24L, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(RedisConst.REGISTER_TIMES_KEY + ip); // 注册次数+1
        }

        /* 30mins激活时间（异步） */
        /* 发送邮件到用户邮箱（异步） */
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.USER_REGISTER_KEY, newUser);
    }

    @Override
    public void activate(String uuid, HttpServletResponse response) throws IOException {
        /* 返回激活结果反馈页面 */
        response.setStatus(200);
        response.setContentType("text/html;charset=utf-8");
        response.setCharacterEncoding("utf-8");

        /* 判断该账号是否存在 */
        User existedUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getActivationUUID, uuid));
        if (existedUser == null) {
            throw new RuntimeException("该账号不存在");
        }
        /* 判断该账号是否已激活 */
        if (existedUser.getActivation() == 1) {
            response.getWriter().print(CommonConst.HTML_ACTIVATION_SUCCESS
                    .replaceAll("<xxxxx>", existedUser.getUsername())
                    .replaceAll("<yyyyy>", CommonConst.USER_LOGIN_URL));
            return; // 账号已激活
        }
        /* 判断该账号是否激活超时 */
        Object t = redisTemplate.opsForValue().get(RedisConst.ACTIVATION_USER_KEY + uuid);
        if (t == null) {
            userMapper.deleteById(existedUser.getId());
            response.getWriter().print(CommonConst.HTML_ACTIVATION_EXPIRED
                    .replaceAll("<yyyyy>", CommonConst.USER_REGISTER_URL));
            return; // 账号激活超时
        }

        Integer userId = (Integer) t; // Redis上没有"Long"这个数据类型

        /* 激活账号 */
        User tt = new User();
        tt.setId((long) userId);
        tt.setActivation(1);
        int result = userMapper.updateById(tt);
        if (result == 1) {
            redisTemplate.delete(RedisConst.ACTIVATION_USER_KEY + uuid);
            response.getWriter().print(CommonConst.HTML_ACTIVATION_SUCCESS
                    .replaceAll("<xxxxx>", existedUser.getUsername())
                    .replaceAll("<yyyyy>", CommonConst.USER_LOGIN_URL));
        }
    }

    @Override
    public User getSimpleUserById(Long userId) {
        String cacheKey = CacheConst.CACHE_USER_KEY + userId;
        return (User) userCache.get(cacheKey, (xx) -> userMapper.getSimpleUserById(userId));
    }

    @Override
    public int follow(Long targetId) {
        //TODO 这2行代码不应该被注释掉
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = user.getId();
//        Long myId = 1L; // 这行代码应该被注释掉

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisConst.USER_FOLLOWING_KEY + myId, targetId))) {
            /* 取消关注target */
            redisTemplate.opsForSet().remove(RedisConst.USER_FOLLOWING_KEY + myId, targetId);
            redisTemplate.opsForSet().remove(RedisConst.USER_FOLLOWER_KEY + targetId, myId);
            return IUserService.UNFOLLOW_SUCCESS;
        } else {
            /* 关注target */
            redisTemplate.opsForSet().add(RedisConst.USER_FOLLOWING_KEY + myId, targetId);
            redisTemplate.opsForSet().add(RedisConst.USER_FOLLOWER_KEY + targetId, myId);
            return IUserService.FOLLOW_SUCCESS;
        }
    }

    @Override
    public FollowingSelectVO getFollowings(Long lastId) {
        //TODO 这2行代码不应该被注释掉
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = user.getId();
//        Long myId = 1L; // 这行代码应该被注释掉

        /* 获取本次查询的followingIds */
        List<User> followings = new ArrayList<>();
        List<Long> followingIds;
        if (lastId == null) {
            Set<Object> range = redisTemplate.opsForZSet().range(RedisConst.USER_FOLLOWING_KEY + myId, 0, IUserService.FOLLOWINGS_STANDARD_PAGE_SIZE - 1);
            followingIds = range.stream().map((id) -> ((Integer) id).longValue()).collect(Collectors.toList());
        } else {
            Long lastIdRank = redisTemplate.opsForZSet().rank(RedisConst.USER_FOLLOWING_KEY + myId, lastId);
            Set<Object> range = redisTemplate.opsForZSet().range(RedisConst.USER_FOLLOWING_KEY + myId, lastIdRank + 1, lastIdRank + IUserService.FOLLOWINGS_STANDARD_PAGE_SIZE);
            followingIds = range.stream().map((id) -> ((Integer) id).longValue()).collect(Collectors.toList());
        }

        /* 查询 */
        if (ObjectUtil.isNotEmpty(followingIds)) {
            for (Long followingId : followingIds) {
                User simpleUserById = this.getSimpleUserById(followingId);
                followings.add(simpleUserById);
            }
        }

        lastId = followings.get(followings.size() - 1).getId();

        FollowingSelectVO followingSelectVO = new FollowingSelectVO();
        followingSelectVO.setFollowings(followings);
        followingSelectVO.setLastId(lastId);
        return followingSelectVO;
    }

    private boolean checkLogin(Serializable userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisConst.LOGIN_USER_KEY
                + userId));
    }
}
