package suzumiya;

import com.github.benmanes.caffeine.cache.Cache;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.constant.CacheConst;
import suzumiya.job.PostScoreUpdateJob;
import suzumiya.job.TableLogicDataClearJob;
import suzumiya.mapper.UserMapper;
import suzumiya.model.pojo.User;
import suzumiya.util.QuartzUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@SpringBootApplication
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {

    @Autowired
    private UserMapper userMapper;

    @Resource(name = "userCache")
    public Cache<String, Object> userCache;

    @PostConstruct
    public void init() {
        // 解决Redis和ES的netty启动冲突问题
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        /* 缓存预热 */
        List<User> simpleUsers = userMapper.getSimpleUsers();
        for (User simpleUser : simpleUsers) {
            userCache.put(CacheConst.CACHE_USER_KEY + simpleUser.getId(), simpleUser);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        /* 发布任务 */
        // 定时刷新post分数（一个小时一次）
        QuartzUtils.addjob("updatePostScoreJob", "updatePostScoreJobGroup", null,
                "updatePostScoreJobTrigger", "updatePostScoreJobTriggerGroup", "0 0 */1 * * ?", PostScoreUpdateJob.class);
        // 定时清除MySQL中已被逻辑删除的数据（一天一次）
        QuartzUtils.addjob("tableLogicDataClearJob", "tableLogicDataClearJobGroup", null,
                "tableLogicDataClearJobTrigger", "tableLogicDataClearJobTriggerGroup", "0 0 0 1/1 * ?", TableLogicDataClearJob.class);
    }
}
