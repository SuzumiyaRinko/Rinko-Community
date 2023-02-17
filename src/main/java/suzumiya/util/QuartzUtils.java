package suzumiya.util;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class QuartzUtils {

    private static Scheduler myScheduler;

    @Resource
    private void setScheduler(Scheduler scheduler) {
        myScheduler = scheduler;
    }

    public static void addjob(String jName, String jGroup, JobDataMap jobDataMap, String tName, String tGroup, String cron, Class<? extends Job> jobClass) {
        // jobDataMap不能为null
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }

        try {
            // 构建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jName, jGroup)
                    .usingJobData(jobDataMap)
                    .build();
            // 按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(tName, tGroup)
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();
            // 启动调度器
            myScheduler.start();
            myScheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception ignore) {
        }
    }

    public static void pausejob(String jName, String jGroup) throws SchedulerException {
        myScheduler.pauseJob(JobKey.jobKey(jName, jGroup));
    }

    public static void resumejob(String jName, String jGroup) throws SchedulerException {
        myScheduler.resumeJob(JobKey.jobKey(jName, jGroup));
    }

    public static void rescheduleJob(String tName, String tGroup, String cron) throws SchedulerException {
        // 表达式调度构建器
        TriggerKey triggerKey = TriggerKey.triggerKey(tName, tGroup);
        CronTrigger trigger = (CronTrigger) myScheduler.getTrigger(triggerKey);
        // 按新的cronExpression表达式重新构建trigger
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
        // 按新的trigger重新设置job执行，重启触发器
        myScheduler.rescheduleJob(triggerKey, trigger);
    }

    public static void deletejob(String jName, String jGroup, String tName, String tGroup) throws SchedulerException {
        myScheduler.pauseTrigger(TriggerKey.triggerKey(tName, tGroup));
        myScheduler.unscheduleJob(TriggerKey.triggerKey(jName, jGroup));
        myScheduler.deleteJob(JobKey.jobKey(jName, jGroup));
    }
}
