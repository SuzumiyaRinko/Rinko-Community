package suzumiya.job;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.PostMapper;
import suzumiya.model.pojo.Post;
import suzumiya.repository.PostRepository;
import suzumiya.service.IPostService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/* 定时更新post的分数 */
@Component
@Slf4j
public class PostSetWonderfulJob extends QuartzJobBean {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate; // RabbitMQ

    @Autowired
    private IPostService postService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostRepository postRepository;

    @Override
    protected void executeInternal(@NotNull JobExecutionContext context) {
        /* 获取top10贴子并加精 */
        List<Post> top10Post = postMapper.getTop10PostWithIdAndIsWonderful();
        List<Post> wonderfulPosts = top10Post.stream().map((post) -> {
            post.setIsWonderful(true);
            return post;
        }).collect(Collectors.toList());

        /* 存储到MySQL中 */
        postService.updateBatchById(wonderfulPosts);

        /* 在定时任务中更新score */
        redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, top10Post.stream().map(Post::getId).toArray(Long[]::new));

        log.debug("贴子加精完成");
    }
}
