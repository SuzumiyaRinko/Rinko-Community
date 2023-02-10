package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.mapper.TagMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.pojo.User;
import suzumiya.service.IPostService;
import suzumiya.service.IUserService;

import java.util.List;

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

    @Autowired
    private IPostService postService;

    @Autowired
    private TagMapper tagMapper;

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
        user.setId(1L);
        user.setIsFamous(true);
        userService.updateById(user);
    }

    // 测试逻辑删除
    @Test
    void testTableLogic() {
//        System.out.println(.getById(5));
    }

    @Test
    void testTagMapper() {
        List<String> names = tagMapper.getAllNameByTagIDs(List.of(1, 2, 4));
        System.out.println(names);
    }

    @Test
    void testUserMapper() {
        System.out.println(Boolean.TRUE.equals(userMapper.getIsFamousByUserId(114514L)));
    }
}
