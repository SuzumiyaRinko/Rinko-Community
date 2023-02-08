package suzumiya.filter;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import suzumiya.constant.RedisConst;
import suzumiya.model.pojo.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component(value = "statisticsFilter")
@Slf4j
public class StatisticsFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String today = LocalDate.now().format(formatter);
        String ip = request.getRemoteHost(); // 获取用户ip
//        String uri = request.getRequestURI(); // 获取uri

        redisTemplate.opsForHyperLogLog().add("uv:" + today, ip); // uv
        redisTemplate.opsForHyperLogLog().add("pv:" + today, ip + "_" + System.currentTimeMillis()); // pv

        /* 尝试获取User并记录dau(每日活跃用户) */
        // 如果是匿名接口，那么JwtFilter中不会存放User对象到SecurityContext中，那么这里的principal就是 String principal = "anonymousUser";
        // 分布式ID太大，无法统计DAU
        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // instanceof 会判null
        if (obj instanceof User) {
            User user = (User) obj;
            // dau
            // 这里的offset虽然是long类型，但是只能传Integer范围内的数（参数传Long类型）
            redisTemplate.opsForValue().setBit("dau:" + today, user.getId(), true);
        }

        filterChain.doFilter(request, response);
    }
}
