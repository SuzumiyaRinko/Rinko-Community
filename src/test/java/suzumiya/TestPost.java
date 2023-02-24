package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.model.dto.PostInsertDTO;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.service.IPostService;

import java.util.List;
import java.util.Set;

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
        for (int i = 1; i <= 50; i++) {
            PostInsertDTO postInsertDTO = new PostInsertDTO();
            postInsertDTO.setTitle("坤坤打飞机后的产后护理");
            postInsertDTO.setContent("坤坤的产后护理是非常麻烦，*((!@!@$#)@_*%@!)@_");
            postInsertDTO.setTagIDs(List.of(1, 3));
            postService.insert(postInsertDTO);
            Thread.sleep(200L);
        }
    }

    @Test
    void testPostSearch() throws NoSuchFieldException, IllegalAccessException {
        PostSearchDTO postSearchDTO = new PostSearchDTO();
        postSearchDTO.setUserId(1L);
        postSearchDTO.setSortType(1);
        postSearchDTO.setSearchKey("坤坤");
        PostSearchVO postSearchVO = postService.search(postSearchDTO);

        System.out.println(postSearchVO);
    }

    @Test
    void testPostDelete() throws InterruptedException {
        postService.delete(1L);
        Thread.sleep(2000L);
    }

    @Test
    void testPostUpdate() {
//        PostUpdateDTO postUpdateDTO = new PostUpdateDTO();
//        postUpdateDTO.setPostId(2L);
//        postUpdateDTO.setTitle("我爱坤坤我爱坤坤");
//        postService.update(postUpdateDTO);
    }

    @Test
    void testSuggest() throws NoSuchFieldException, IllegalAccessException {
        Set<String> suggestions = postService.suggest("坤坤");
        System.out.println(suggestions);
    }

    @Test
    void testSetWonderful() {
        postService.setWonderful(List.of(1L));
    }

    @Test
    void testLike() throws InterruptedException {
        postService.like(2L);
        while (true) {
        }
    }

    @Test
    void testCollect() throws InterruptedException {
        postService.collect(2L);
        while (true) {
        }
    }
}
