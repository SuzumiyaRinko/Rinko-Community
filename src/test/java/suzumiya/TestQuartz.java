package suzumiya;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.quartz.SchedulerException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.job.PostScoreUpdateJob;
import suzumiya.util.QuartzUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestQuartz {

    @Test
    void testAddTask() throws InterruptedException {
        QuartzUtils.addjob("updatePostScoreJob", "updatePostScoreJobGroup", null,
                "updatePostScoreJobTrigger", "updatePostScoreJobTriggerGroup", "0 0 */1 * * ?", PostScoreUpdateJob.class);

        while (true) {
            Thread.sleep(2000L);
        }
    }

    @Test
    void testDeleteTask() throws SchedulerException {
        QuartzUtils.deletejob("updatePostScoreJob", "updatePostScoreJobGroup",
                "updatePostScoreJobTrigger", "updatePostScoreJobTriggerGroup");
        QuartzUtils.deletejob("tableLogicDataClearJob", "tableLogicDataClearJobGroup",
                "tableLogicDataClearJobTrigger", "tableLogicDataClearJobTriggerGroup");
    }

    @Test
    void testPostConstruct() throws InterruptedException {
        while (true) {
            Thread.sleep(2000L);
        }
    }
}
