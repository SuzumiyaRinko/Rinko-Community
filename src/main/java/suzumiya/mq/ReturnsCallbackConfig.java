package suzumiya.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration("returnsCallbackConfig")
@Slf4j
public class ReturnsCallbackConfig implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 设置ReturnCallback
        rabbitTemplate.setReturnsCallback((returned -> {
            // 判断是否为DelayExchange的延迟消息
            if(returned.getMessage().getMessageProperties().getReceivedDelay() != null) {
                return; // 延迟消息不触发ReturnCallback
            }
            log.info("消息路由失败，应答码: {}, 原因: {}, 交换机: {}, 路由键: {}, 消息: {}",
                    returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), returned.getMessage().toString());
        }));
    }
}
