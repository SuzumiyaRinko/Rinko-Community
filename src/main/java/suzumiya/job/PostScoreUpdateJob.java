package suzumiya.job;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import suzumiya.constant.CacheConst;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.PostMapper;
import suzumiya.model.pojo.Post;
import suzumiya.repository.PostRepository;

import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/* 定时更新post的分数 */
@Component
@Slf4j
public class PostScoreUpdateJob extends QuartzJobBean {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate; // RabbitMQ

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostRepository postRepository;

    @Override
    protected void executeInternal(@NotNull JobExecutionContext context) {
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
                        + (post.getCreateTime().toEpochSecond(ZoneOffset.of("+8")) - CommonConst.COMMUNITY_EPOCH) * 1.0 / (60 * 60 * 24L);
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

            /* 清除post缓存（异步） */
            rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.CACHE_CLEAR_KEY, CacheConst.CACHE_POST_KEY_PATTERN);
        }

        log.debug("为post列表刷新score完成");
    }
}
