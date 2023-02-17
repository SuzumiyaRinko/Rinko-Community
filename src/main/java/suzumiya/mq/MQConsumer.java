package suzumiya.mq;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.model.dto.CacheClearDTO;
import suzumiya.model.dto.CacheUpdateDTO;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.User;
import suzumiya.repository.PostRepository;
import suzumiya.service.ICacheService;
import suzumiya.service.IMessageService;
import suzumiya.util.MailUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class MQConsumer {

    @Autowired
    private ICacheService cacheService;

    @Autowired
    private IMessageService messageService;

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
    public void listenMessageInsertQueue(MessageInsertDTO messageInsertDTO) {
        /* 发送消息 */
        messageService.sendMessage(messageInsertDTO);
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
