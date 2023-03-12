package suzumiya.mq;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.CommentMapper;
import suzumiya.mapper.MessageMapper;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.CacheClearDTO;
import suzumiya.model.dto.CacheUpdateDTO;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.dto.UserUnfollowDTO;
import suzumiya.model.pojo.User;
import suzumiya.repository.PostRepository;
import suzumiya.service.ICacheService;
import suzumiya.service.IMessageService;
import suzumiya.util.MailUtils;
import suzumiya.util.TestFTPUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class MQConsumer {

    @Autowired
    private ICacheService cacheService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Resource(name = "userCache")
    private Cache<String, Object> userCache; // Caffeine

    @Resource(name = "postCache")
    private Cache<String, Object> postCache; // Caffeine

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PostRepository postRepository;

    /* 监听用户注册接口 */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.USER_REGISTER_QUEUE),
            exchange = @Exchange(name = MQConstant.SERVICE_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
            key = {MQConstant.USER_REGISTER_KEY}
    ))
    public void listenUserRegisterQueue(User newUser) throws MessagingException {
        /* 发送邮件到用户邮箱 */
        String toMail = newUser.getUsername();
        String activationURL = CommonConst.PREFIX_ACTIVATION_URL + newUser.getActivationUUID();
        String text = CommonConst.HTML_ACTIVATION.replaceAll("<xxxxx>", toMail).replaceAll("<yyyyy>", activationURL);

        MailUtils.sendMail(CommonConst.MAIL_FROM, List.of(toMail), "Rinko-Community | 账号激活", null, text, null);

        /* 30mins激活时间 */
        redisTemplate.opsForValue().set(RedisConst.ACTIVATION_USER_KEY + newUser.getActivationUUID(), newUser.getId(), 30L, TimeUnit.MINUTES); // 30mins

        log.debug("正在注册 username={} ", newUser.getUsername());
    }

    /* 监听用户Unfollow */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.USER_UNFOLLOW_QUEUE),
            exchange = @Exchange(name = MQConstant.SERVICE_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
            key = {MQConstant.USER_UNFOLLOW_KEY}
    ))
    public void listenUserUnfollowQueue(UserUnfollowDTO userUnfollowDTO) {
        /* 在自己的Feed流中移除对方的数据 */
        Long myUserId = userUnfollowDTO.getMyUserId();
        Long targetId = userUnfollowDTO.getTargetId();
        Set<Object> t = redisTemplate.opsForZSet().range(RedisConst.USER_FEED_KEY + myUserId, 0, -1);
        for (Object tt : t) {
            long postId = ((Integer) tt).longValue();
            if (targetId.equals(postMapper.getUserIdByPostId(postId))) {
                redisTemplate.opsForZSet().remove(RedisConst.USER_FEED_KEY + myUserId, postId);
            }
        }
    }

    /* 监听Cache更新接口 */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.CACHE_UPDATE_QUEUE),
            exchange = @Exchange(name = MQConstant.SERVICE_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
            key = {MQConstant.CACHE_UPDATE_KEY}
    ))
    public void listenCacheUpdateQueue(CacheUpdateDTO cacheUpdateDTO) {
        /* 构建或刷新Caffeine和Redis缓存 */
        cacheService.updateCache(cacheUpdateDTO);

        log.debug("构建或刷新Caffeine和Redis缓存");
    }

    /* 监听Cache清除接口 */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.CACHE_CLEAR_QUEUE),
            exchange = @Exchange(name = MQConstant.SERVICE_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
            key = {MQConstant.CACHE_CLEAR_KEY}
    ))
    public void listenCacheClearQueue(CacheClearDTO cacheClearDTO) {
        /* 清除Caffeine和Redis缓存 */
        cacheService.clearCache(cacheClearDTO);

        log.debug("清除Caffeine和Redis缓存, keyPattern={}", cacheClearDTO.getKeyPattern());
    }

    /* 监听Message发送接口 */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.MESSAGE_INSERT_QUEUE),
            exchange = @Exchange(name = MQConstant.SERVICE_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
            key = {MQConstant.MESSAGE_INSERT_KEY}
    ))
    public void listenMessageInsertQueue(MessageInsertDTO messageInsertDTO) throws JsonProcessingException {
        /* 发送消息 */
        messageService.saveMessage(messageInsertDTO);
    }

    /* 监听Post删除接口 */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.POST_DELETE_QUEUE),
            exchange = @Exchange(name = MQConstant.SERVICE_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
            key = {MQConstant.POST_DELETE_KEY}
    ))
    public void listenPostDeleteQueue(Long postId) {
        /* 在FTP中删除该post的pictures */
        String pictures = postMapper.getPicturesByPostId(postId);
        if (StrUtil.isNotBlank(pictures)) {
            String[] picturesSplit = pictures.split("\\|");
            for (String filePath : picturesSplit) {
                TestFTPUtils.deleteFile(filePath);
            }
        }

        /* 删除所有comment, comment的pictures, 以及其相关recomment, recomment的pictures */
        List<Long> commentIDs = commentMapper.getAllCommentIdByPostId(postId);
        for (Long commentID : commentIDs) {
            List<Long> recommentIDs = commentMapper.getAllRecommentIdByCommentId(commentID);
            if (ObjectUtil.isNotEmpty(recommentIDs)) {
                commentMapper.deleteBatchIds(recommentIDs);
                for (Long recommentID : recommentIDs) {
                    /* 在FTP中删除该recomment的pictures */
                    pictures = commentMapper.getPicturesByCommentId(recommentID);
                    if (StrUtil.isNotBlank(pictures)) {
                        String[] picturesSplit = pictures.split("\\|");
                        for (String filePath : picturesSplit) {
                            TestFTPUtils.deleteFile(filePath);
                        }
                    }
                }
            }
        }
        if (ObjectUtil.isNotEmpty(commentIDs)) {
            commentMapper.deleteBatchIds(commentIDs);
            for (Long commentID : commentIDs) {
                /* 在FTP中删除该comment的pictures */
                pictures = commentMapper.getPicturesByCommentId(commentID);
                if (StrUtil.isNotBlank(pictures)) {
                    String[] picturesSplit = pictures.split("\\|");
                    for (String filePath : picturesSplit) {
                        TestFTPUtils.deleteFile(filePath);
                    }
                }
            }
        }

        /* 删除该post的like相关数据 */
        redisTemplate.delete(RedisConst.POST_LIKE_LIST_KEY + postId);
        redisTemplate.delete(RedisConst.POST_LIKE_COUNT_KEY + postId);

        /* 删除该post的collection相关数据 */
        Set<Object> t = redisTemplate.opsForSet().members(RedisConst.POST_COLLECTION_LIST_KEY + postId);
        List<Integer> userIDs = t.stream().map((el) -> (Integer) el).collect(Collectors.toList());
        for (Integer userID : userIDs) {
            redisTemplate.opsForZSet().remove(RedisConst.USER_COLLECTIONS_KEY + userID, postId);
        }
        redisTemplate.delete(RedisConst.POST_COLLECTION_LIST_KEY + postId);
        redisTemplate.delete(RedisConst.POST_COLLECTION_COUNT_KEY + postId);
    }

    /* 监听公共聊天室unreadCount++ */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.MESSAGE_PUBLIC_UNREAD_QUEUE),
            exchange = @Exchange(name = MQConstant.SERVICE_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
            key = {MQConstant.MESSAGE_PUBLIC_UNREAD_KEY}
    ))
    public void listenPublicUnreadQueue(Long myUserId) {
        List<Long> userIDs = userMapper.getAllUserId();

        // userIDs > 2500之后, pipelined占优
        if (userIDs.size() < 2500) {
            for (Long userID : userIDs) {
                if (!userID.equals(myUserId)) {
                    redisTemplate.opsForHash().increment(RedisConst.USER_UNREAD_KEY + userID, "0", 1L);
                }
            }
        } else {
            redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    for (Long userID : userIDs) {
                        if (!userID.equals(myUserId)) {
                            connection.hIncrBy((RedisConst.USER_UNREAD_KEY + userID).getBytes(StandardCharsets.UTF_8), "0".getBytes(StandardCharsets.UTF_8), 1L);
                        }
                    }
                    return null;
                }
            });
        }
    }


    // DelayQueue：监听用户激活时间是否结束
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = MQConstant.ACTIVATION_QUEUE),
//            exchange = @Exchange(name = MQConstant.DELAY_DIRECT, type = ExchangeTypes.DIRECT, delayed = "true"),
//            key = {MQConstant.ACTIVATION_KEY}
//    ))
//    public void listenActivationQueue(Message message) {
//        String uuid = new String(message.getBody());
//        Integer userId = (Integer) redisTemplate.opsForValue().get(RedisConst.ACTIVATION_USER_KEY + uuid);
//        User user = userMapper.getUserById((long) userId);
//        userMapper.deleteById(userId);
//
//        log.debug("正在取消 userId={} 的激活资格", user.getUsername());
//
//        /* 取消激活资格 */
//        redisTemplate.delete(RedisConst.ACTIVATION_USER_KEY + uuid);
//    }
}
