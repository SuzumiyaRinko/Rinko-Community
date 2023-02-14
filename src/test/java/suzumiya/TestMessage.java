package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.service.IMessageService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestMessage {

    @Autowired
    private IMessageService messageService;

    @Test
    void testSendMessage() {
        MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
        messageInsertDTO.setFromUserId(114514L);
        messageInsertDTO.setToUserId(1L);
        messageInsertDTO.setContent("这是消息正文");
        messageService.sendMessage(messageInsertDTO);
    }
}
