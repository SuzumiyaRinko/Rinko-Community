package suzumiya.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("mqConfig")
@Slf4j
public class MQConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        /* 设置ReturnCallback */
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        rabbitTemplate.setReturnsCallback((returned -> {
            // 判断是否为DelayExchange的延迟消息
            if (returned.getMessage().getMessageProperties().getReceivedDelay() != null) {
                return; // 延迟消息不触发ReturnCallback
            }
            log.info("消息路由失败，应答码: {}, 原因: {}, 交换机: {}, 路由键: {}, 消息: {}",
                    returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), returned.getMessage().toString());
        }));
        /* 让消息在Queue中以JSON字符串的形式存储 */
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
