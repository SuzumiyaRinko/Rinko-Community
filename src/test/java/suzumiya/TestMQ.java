package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
// @EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestMQ {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    void testTTL() {
        // 交换机
        // convertAndSend(String exchange, String routingKey, Object message)
        String content = "msg";
        Message message = MessageBuilder.withBody(content.getBytes(StandardCharsets.UTF_8)).setHeader("x-delay", 1000).build(); // 1s
        rabbitTemplate.convertAndSend("juejue.direct", "juejue", message);


    }
}
