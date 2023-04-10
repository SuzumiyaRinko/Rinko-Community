package suzumiya;

import com.github.benmanes.caffeine.cache.Cache;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.job.PostScoreUpdateJob;
import suzumiya.job.TableLogicDataClearJob;
import suzumiya.mapper.UserMapper;
import suzumiya.util.QuartzUtils;

import javax.annotation.Resource;

@SpringBootApplication
@MapperScan(basePackages = "suzumiya.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {

    private static UserMapper userMapper;

    public static Cache<String, Object> userCache;

    @Resource
    public void setUserMapper(UserMapper userMapper) {
        Application.userMapper = userMapper;
    }

    @Resource(name = "userCache")
    public void setUserCache(Cache<String, Object> userCache) {
        Application.userCache = userCache;
    }

    public static String clusterNode;

    @Value("${commons.clusterNode}")
    public void setClusterNode(String clusterNode) {
        Application.clusterNode = clusterNode;
    }

    public static String testStr;

    @Value("${testStr}")
    public void setTestStr(String testStr) {
        Application.testStr = testStr;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        /* 解决Redis和ES的netty启动冲突问题 */
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        /* SimpleUser缓存预热 */
//        List<User> simpleUsers = userMapper.getSimpleUsers();
//        for (User simpleUser : simpleUsers) {
//            simpleUser.setRoles(userMapper.getRolesByUserId(simpleUser.getId()));
//            userCache.put(CacheConst.CACHE_USER_KEY + simpleUser.getId(), simpleUser);
//        }

        /* 发布任务 */
        // 定时刷新post分数（一个小时一次）
        QuartzUtils.addjob("updatePostScoreJob", "updatePostScoreJobGroup", null,
                "updatePostScoreJobTrigger", "updatePostScoreJobTriggerGroup", "0 0 */1 * * ?", PostScoreUpdateJob.class);
        // 定时清除MySQL中已被逻辑删除的数据（15天一次, 凌晨3点开始）
        QuartzUtils.addjob("tableLogicDataClearJob", "tableLogicDataClearJobGroup", null,
                "tableLogicDataClearJobTrigger", "tableLogicDataClearJobTriggerGroup", "0 0 3 1/15 * ?", TableLogicDataClearJob.class);
        // 定时清除匿名用户（5天一次, 凌晨3点开始）
        // 匿名用户不能被清除, 否则根据postUserId查询的用户为null, 会出问题
//        QuartzUtils.addjob("anonymousUserClearJob", "anonymousUserClearJobGroup", null,
//                "anonymousUserClearJobTrigger", "anonymousUserClearJobTriggerGroup", "0 0 3 1/5 * ?", AnonymousUserClearJob.class);

        System.out.println("api started successfully.");
        System.out.println("clusterNode: " + clusterNode);
        System.out.println("testStr: " + testStr);
    }
}
