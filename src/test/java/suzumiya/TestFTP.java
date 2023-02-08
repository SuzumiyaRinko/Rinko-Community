package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import suzumiya.util.FTPUtils;

import java.io.IOException;
import java.io.InputStream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
// @EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestFTP {

    @Test
    void testFTP() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/1.jpg");
        boolean result = FTPUtils.uploadFile(resource.getFilename(), resource.getInputStream());
        System.out.println("result = " + result);
    }
}
