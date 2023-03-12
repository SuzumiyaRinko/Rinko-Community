package suzumiya;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import suzumiya.constant.CacheConst;
import suzumiya.model.dto.BadWordQueryDTO;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.service.IPostService;
import suzumiya.util.SuzumiyaUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IPostService postService;

    @Resource(name = "postCache")
    private Cache<String, Object> postCache; // Caffeine

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    void test() {
        System.out.println(passwordEncoder.encode("12345678114514"));
    }

    @Test
    void testPostCaffeine() throws NoSuchFieldException, IllegalAccessException {
        PostSearchDTO postSearchDTO = new PostSearchDTO();
        postSearchDTO.setIsSuggestion(false);
        postSearchDTO.setIsSearchMyself(false);
        postSearchDTO.setSortType(1);
        postSearchDTO.setPageNum(1);
        PostSearchVO search = postService.search(postSearchDTO);

        postCache.put("key", search);
        // Caffeine
        Object t = postCache.getIfPresent("key");
        if (t != null) {
            PostSearchVO result;
            result = (PostSearchVO) t;
            System.out.println("result: " + result);
        }
    }

    @Test
    void testTTL() {
        redisTemplate.opsForValue().set("testTTL", 1);
        redisTemplate.expire("testTTL", CacheConst.CACHE_REDIS_USER_TTL());
    }

    @Test
    void testRestTemplate() throws JsonProcessingException {
        String url = SuzumiyaUtils.BAD_WORD_FILTER_URL;
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("key", SuzumiyaUtils.BAD_WORD_FILTER_KEY);
        requestBody.add("word", "cnm啊啊啊啊啊习近平哼哼哼啊啊啊啊啊啊毛泽东哼哼哼啊啊啊啊啊啊");
        String result = restTemplate.postForObject(url, requestBody, String.class);

        JsonNode jsonNode = objectMapper.readTree(result);
        String sensitiveWordsArr = jsonNode.findValue("data").findValue("data").toString();
        sensitiveWordsArr = sensitiveWordsArr.substring(1, sensitiveWordsArr.length() - 1);
        sensitiveWordsArr = sensitiveWordsArr.replaceAll("},\\{", "}{");

        List<String> jsonStrs = new ArrayList<>();
        int index;
        while ((index = sensitiveWordsArr.indexOf("}")) != -1) {
            String t = sensitiveWordsArr.substring(0, index + 1);
            sensitiveWordsArr = sensitiveWordsArr.substring(index + 1);
            jsonStrs.add(t);
        }

        for (String jsonStr : jsonStrs) {
            BadWordQueryDTO badWordQueryDTO = objectMapper.readValue(jsonStr, BadWordQueryDTO.class);
            System.out.println(badWordQueryDTO);
        }
    }
}
