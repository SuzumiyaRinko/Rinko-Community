package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.mapper.UserMapper;
import suzumiya.model.pojo.User;
import suzumiya.service.IUserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
// @EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestMySQL {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IUserService userService;

    @Test
    void testMySQL() {
        User user = userMapper.getUserById(1L);
        System.out.println(user);
    }

    @Test
    void testInsert() {
        User user = new User();
        user.setUsername("123");
        user.setPassword("456");
        user.setNickname("juejue");
        userService.save(user);
        System.out.println(user);
    }

    // 增量更新
    @Test
    void testUpdate() {
        User user = new User();
        user.setId(1622138408061227009L);
        user.setNickname("okokok");
        userService.updateById(user);
    }
}
