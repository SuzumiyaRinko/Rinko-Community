package suzumiya;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.*;
import suzumiya.constant.CommonConst;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.PostMapper;
import suzumiya.model.pojo.Post;
import suzumiya.repository.PostRepository;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestRedis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostRepository postRepository;

    @Test
    void testPostUpdateScore() {
        BoundSetOperations<String, Object> boundOperations = redisTemplate.boundSetOps(RedisConst.POST_SCORE_UPDATE_KEY);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        /* 获取需要刷新score的post */
        Set<Object> members = boundOperations.members();
        if (ObjectUtil.isNotEmpty(members)) {
            List<Integer> postIds = members.stream().map((el) -> (Integer) el).collect(Collectors.toList());
            List<Post> posts = postMapper.getPostsForUpdatingScore(postIds);
            for (Post post : posts) {
                // 获取like, comment, collection数
                long postId = post.getId();
                Object tmpLikeCount = valueOperations.get(RedisConst.POST_LIKE_COUNT_KEY + postId);
                Object tmpCommentCount = valueOperations.get(RedisConst.POST_COMMENT_COUNT_KEY + postId);
                Object tmpCollectionCount = valueOperations.get(RedisConst.POST_COLLECTION_COUNT_KEY + postId);
                int likeCount = 0;
                int commentCount = 0;
                int collectionCount = 0;
                if (tmpLikeCount != null) likeCount = (int) tmpLikeCount;
                if (tmpCommentCount != null) commentCount = (int) tmpCommentCount;
                if (tmpCollectionCount != null) collectionCount = (int) tmpCollectionCount;
                Boolean wonderful = post.getIsWonderful();

                // 计算分数并更新MySQL和ES
                double newScore = Math.log(Math.max(1, (Boolean.TRUE.equals(wonderful) ? 75 : 0) + likeCount * 2 + commentCount * 10 + collectionCount * 2))
                        + (post.getCreateTime().toEpochSecond(ZoneOffset.of("+8")) - CommonConst.COMMUNITY_EPOCH);
                post.setScore(newScore);
                // MySQL
                postMapper.updateById(post);
                // ES
                Optional<Post> optional = postRepository.findById(postId);
                if (optional.isEmpty()) {
                    throw new RuntimeException("该post不存在");
                }
                Post t = optional.get();
                t.setScore(newScore);
                postRepository.save(t);
                // 在set集合中去掉该postId
                boundOperations.remove(postId);
            }

            /* 清除Caffeine和Redis中的缓存（异步） */
        }
    }

    @Test
    void testCacheClear() {
        Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match("cache:post:*").build());
        while (cursor.hasNext()) {
            String key = cursor.next();
            System.out.println(key);
        }
        System.out.println();
    }

    @Test
    void testMGet() {
    }
}
