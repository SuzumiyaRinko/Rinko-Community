package suzumiya.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import suzumiya.constant.CacheConst;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.CommentMapper;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.CacheUpdateDTO;
import suzumiya.model.dto.CommentInsertDTO;
import suzumiya.model.dto.CommentSelectDTO;
import suzumiya.model.pojo.Comment;
import suzumiya.model.pojo.Post;
import suzumiya.model.pojo.User;
import suzumiya.service.ICommentService;
import suzumiya.util.WordTreeUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate; // RabbitMQ

    @Resource(name = "userCache")
    private Cache<String, Object> userCache; // Caffeine

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public void comment(CommentInsertDTO commentInsertDTO) {
        /* 判断内容长度 */
        if (commentInsertDTO.getContent().length() > 2000) {
            throw new RuntimeException("内容长度超出限制");
        }

        Comment comment = new Comment();

        /* 过滤敏感词 */
        comment.setContent(WordTreeUtils.replaceAllSensitiveWords(commentInsertDTO.getContent()));

        /* 清除HTML标记 */
//        comment.setContent(HtmlUtil.cleanHtmlTag(comment.getContent()));

        /* 新增comment到MySQL */

        //TODO 这两行代码不应该被注释掉
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        comment.setUserId(user.getId());
        comment.setUserId(1L); // 这行代码应该被注释掉

        comment.setTargetType(commentInsertDTO.getType());
        comment.setTargetId(commentInsertDTO.getTargetId());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        if (commentInsertDTO.getType() == CommentInsertDTO.COMMENT_TYPE_2POST) {
            /* 添加到待算分Post的Set集合 */
            redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, commentInsertDTO.getTargetId());
        }
    }

    @Override
    public void delete(Long commentId) {
        /* 在MySQL把comment逻辑删除 */
        commentMapper.deleteById(commentId);

        /* 添加到待算分Post的Set集合 */
        redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, commentId);
    }

    @Override
    public PageInfo<Comment> select(CommentSelectDTO commentSelectDTO) {
        /* targetId判空 */
        if (commentSelectDTO.getTargetId() == null) {
            throw new RuntimeException("targetId为null");
        }

        Long targetId = commentSelectDTO.getTargetId();
        Integer selectType = commentSelectDTO.getSelectType();
        Integer targetType = commentSelectDTO.getTargetType();
        int sortType = commentSelectDTO.getSortType();
        int pageNum = commentSelectDTO.getPageNum();

        /* 查询结果并返回 */
        // 1 判断targetType
        Post existedPost = null;
        Comment existedComment = null;
        if (targetType == CommentSelectDTO.TARGET_TYPE_POST) {
            // 对post的评论
            existedPost = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getId, targetId));
            if (existedPost == null) {
                throw new RuntimeException("要评论的post不存在");
            }
        } else {
            // 对comment的评论
            existedComment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>().eq(Comment::getId, targetId));
            if (existedComment == null) {
                throw new RuntimeException("要评论的comment不存在");
            }
        }

        // 2 查询结果并返回
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        // 2.1 comment的target类型
        queryWrapper.eq(Comment::getTargetType, targetType);
        // 如果是对post的评论
        if (targetType == CommentSelectDTO.TARGET_TYPE_POST) {
            // 判断查询类型是 "所有"还是"只看楼主"
            if (selectType == CommentSelectDTO.SELECT_TYPE_POSTER) {
                queryWrapper.eq(Comment::getUserId, existedPost.getUserId());
            }
            // 判断查询的时间顺序
            if (sortType == CommentSelectDTO.SORT_TYPE_REVERSE) {
                queryWrapper.orderByDesc(Comment::getCreateTime);
            }
        }
        // 2.2 分页
        PageHelper.startPage(pageNum, CommonConst.STANDARD_PAGE_SIZE);
        // 2.3 查询
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        // 2.4 获取User信息
        for (Comment comment : comments) {
            Long userId = comment.getUserId();
            String cacheKey = CacheConst.CACHE_USER_KEY + userId;
            boolean flag = false;
            User user = null;
            // 2.4.1 查询缓存
            // Caffeine
            Object t = userCache.getIfPresent(cacheKey);
            if (t != null) {
                user = (User) t;
                flag = true;
            }

            // Redis
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(cacheKey);
            if (ObjectUtil.isNotEmpty(entries)) {
                BeanUtil.fillBeanWithMap(entries, user, null);
                flag = true;
            }

            // DB
            if (!flag) {
                user = userMapper.getSimpleUserById(userId);
            }

            comment.setCommentUser(user);

            // 2.4.2 构建或刷新Caffeine和Redis缓存（异步）
            CacheUpdateDTO cacheUpdateDTO = new CacheUpdateDTO();
            cacheUpdateDTO.setCacheType(CacheConst.VALUE_TYPE_POJO);
            cacheUpdateDTO.setKey(cacheKey);
            cacheUpdateDTO.setValue(user);
            cacheUpdateDTO.setCaffeineType(CacheConst.CAFFEINE_TYPE_USER);
            cacheUpdateDTO.setRedisTTL(Duration.ofMinutes(30L));
            rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.CACHE_UPDATE_KEY, cacheUpdateDTO);
        }
        // 2.5 判断是否需要返回targetId的前3条comment
        if (targetType == CommentSelectDTO.TARGET_TYPE_POST) {
            for (Comment comment : comments) {
                Long id = comment.getId();
                List<String> first3Comments = commentMapper.getFirst3CommentsByTargetId(id);
                comment.setFirst3Comments(first3Comments);
            }
        }

        // 3 返回结果
        return new PageInfo<>(comments);
    }
}
