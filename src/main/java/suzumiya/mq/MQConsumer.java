package suzumiya.mq;

import cn.hutool.core.lang.UUID;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.impl.AMQImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.UserMapper;
import suzumiya.model.pojo.User;
import suzumiya.util.MailUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class MQConsumer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserMapper userMapper;

    // 监听用户注册接口
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
//        Message message = MessageBuilder.withBody(uuid.getBytes(StandardCharsets.UTF_8)).setHeader("x-delay", 1000 * 60 * 30).build(); // 30mins
//        rabbitTemplate.convertAndSend(MQConstant.DELAY_DIRECT, MQConstant.ACTIVATION_KEY, message);

        log.debug("正在注册 username={} ", newUser.getUsername());
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
