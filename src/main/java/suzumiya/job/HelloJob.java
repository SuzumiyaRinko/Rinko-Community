//package suzumiya.job;
//
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.quartz.JobExecutionContext;
//import org.springframework.scheduling.quartz.QuartzJobBean;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Slf4j
//public class HelloJob extends QuartzJobBean {
//
//    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    @Override
//    protected void executeInternal(@NotNull JobExecutionContext context) {
//        log.info("HelloJob执行时间: " + LocalDateTime.now().format(timeFormatter));
//        log.info("data: {}", context.getMergedJobDataMap().get("data"));
//    }
//}
