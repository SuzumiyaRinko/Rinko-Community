package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.constant.CommonConst;
import suzumiya.util.MailUtils;

import javax.mail.MessagingException;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
// @EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestMail {

    @Test
    void testMail() throws MessagingException {
        Long time1 = System.currentTimeMillis();
        MailUtils.sendMail("Txz2018911711@163.com", List.of("3233219183@qq.com"), "202302060932", null, CommonConst.HTML_ACTIVATION_SUCCESS, null);
        Long time2 = System.currentTimeMillis();
        System.out.println((time2-time1) + "ms");
    }
}
