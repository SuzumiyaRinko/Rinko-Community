package suzumiya.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.UserMapper;

/* 定时清除MySQL中已被逻辑删除的数据 */
@Component
@Slf4j
public class TableLogicDataClearJob extends QuartzJobBean {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        userMapper.tableLogicDataClear();
        postMapper.tableLogicDataClear();

        log.debug("正在清除MySQL中已被逻辑删除的数据");
    }
}
