package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.model.vo.FollowingSelectVO;
import suzumiya.service.IUserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestUser {

    @Autowired
    private IUserService userService;

    @Test
    void testGetFollowings() {
        Long time1 = System.currentTimeMillis();
        FollowingSelectVO followingSelectVO = userService.getFollowings(null);
        Long time2 = System.currentTimeMillis();
        System.out.println(followingSelectVO.getFollowings());
        System.out.println(followingSelectVO.getLastId());
        System.out.println("耗时：" + (time2 - time1));

        while (true) {
        }
    }
}
