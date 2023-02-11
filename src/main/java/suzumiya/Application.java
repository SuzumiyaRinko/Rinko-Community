package suzumiya;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.job.PostScoreUpdateJob;
import suzumiya.job.TableLogicDataClearJob;
import suzumiya.util.QuartzUtils;

import javax.annotation.PostConstruct;

@SpringBootApplication
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {

    @PostConstruct
    public void init() {
        // 解决Redis和ES的netty启动冲突问题
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
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
