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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.BaseResponse;

import javax.servlet.http.HttpServletRequest;
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
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        String ip = request.getRemoteHost();
        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username =  (obj instanceof User) ? ((User) obj).getUsername() : "null"; // instanceof 本来就会判断obj是否为null

        log.debug("IP: {}, username: {}, time: {}, API: {}", ip, username, time, joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
//        log.info("typeName: {}", joinPoint.getSignature().getDeclaringTypeName());
//        log.info("args: {}", (Object[]) ((MethodSignature)joinPoint.getSignature()).getParameterTypes());
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
