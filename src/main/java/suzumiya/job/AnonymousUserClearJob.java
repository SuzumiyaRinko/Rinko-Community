package suzumiya.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import suzumiya.mapper.UserMapper;
import suzumiya.model.pojo.User;

/* 定时清除MySQL中已被逻辑删除的数据 */
@Component
@Slf4j
public class AnonymousUserClearJob extends QuartzJobBean {

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        userMapper.delete(new LambdaQueryWrapper<User>()
                .eq(User::getActivation, 2));

        log.debug("正在逻辑删除匿名用户");
    }
}
