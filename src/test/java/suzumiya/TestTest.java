package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void test() {
        System.out.println(passwordEncoder.encode("12345678114514"));
    }
}
