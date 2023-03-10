package suzumiya.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.BaseResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Aspect
@Slf4j
public class ControllerAspect {

    @Pointcut("execution(* suzumiya.controller.*.*(..))")
    public void controllerPointcut() {
    }

    /* 统一打印日志 */
    @Before("controllerPointcut()")
    public void log(JoinPoint joinPoint) {
        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username =  (obj instanceof User) ? ((User) obj).getUsername() : "null"; // instanceof 本来就会判断obj是否为null

        log.debug("username: {}, time: {}, API: {}", username, time, joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
    }

    /* 记录请求共消耗的时间 */
    @Around("controllerPointcut()")
    public BaseResponse recordCost(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        BaseResponse response = (BaseResponse) joinPoint.proceed(); // 异常统一由GlobalExceptionHandler处理
        long end = System.currentTimeMillis();
        log.debug("API {} takes {}ms", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName(), (end-start));
        return response;
    }
}
