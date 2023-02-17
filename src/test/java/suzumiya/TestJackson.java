package suzumiya;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.constant.CacheConst;
import suzumiya.model.dto.CacheUpdateDTO;
import suzumiya.model.vo.PostSearchVO;

import javax.annotation.Resource;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
// @EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestJackson {

    @Resource
    private ObjectMapper objectMapper;

    @Test
    void testJackson() throws JsonProcessingException {
        CacheUpdateDTO cacheUpdateDTO = new CacheUpdateDTO();
        cacheUpdateDTO.setCacheType(CacheConst.VALUE_TYPE_POJO);
        cacheUpdateDTO.setKey("cacheKey");
        cacheUpdateDTO.setValue(new PostSearchVO());
        cacheUpdateDTO.setCaffeineType(CacheConst.CAFFEINE_TYPE_POST);
        cacheUpdateDTO.setDuration(Duration.ofMinutes(30L));
        System.out.println(objectMapper.writeValueAsString(cacheUpdateDTO));
    }
}
