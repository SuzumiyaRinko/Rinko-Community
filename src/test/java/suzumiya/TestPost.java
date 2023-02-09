package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.service.IPostService;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestPost {

    @Autowired
    private IPostService postService;

    @Test
    void testPostInsert() throws InterruptedException {
        Post post = new Post();
        post.setUserId(2L);
        post.setTitle("坤坤打飞机后的产后护理");
        post.setContent("坤坤的产后护理是非常麻烦的麻烦的傻逼xxx打飞机芜湖");
        post.setTagIDs(List.of(1, 3));
        postService.insert(post);

        while (true) {
            Thread.sleep(1000L);
        }
    }

    @Test
    void testPostSearch() throws NoSuchFieldException, IllegalAccessException {
        PostSearchDTO postSearchDTO = new PostSearchDTO();
        postSearchDTO.setSortType(2);
//        postSearchDTO.setSearchKey("坤坤");
        PostSearchVO postSearchVO = postService.search(postSearchDTO);
        System.out.println(postSearchVO.getAggregation());
//        System.out.println(postSearchVO.getPage().getData());
    }
}
