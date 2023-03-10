package suzumiya.filter;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import suzumiya.model.pojo.User;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component(value = "statisticsFilter")
@Slf4j
public class StatisticsFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    private final List<String> anonymousURIs = new ArrayList<>();

    @PostConstruct
    private void init() {
        anonymousURIs.add(SERVLET_CONTEXT + "/verifyCode");
        anonymousURIs.add(SERVLET_CONTEXT + "/user/login");
        anonymousURIs.add(SERVLET_CONTEXT + "/user/loginAnonymously");
        anonymousURIs.add(SERVLET_CONTEXT + "/user/register");
        anonymousURIs.add(SERVLET_CONTEXT + "/user/activation");
    }

    private static final String SERVLET_CONTEXT = "";

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        /* 可匿名访问, 不统计任何数据 */
        String uri = request.getRequestURI();
        for (String anonymousURI : anonymousURIs) {
            if (uri.equals(anonymousURI) || uri.startsWith(anonymousURI)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();

        String today = LocalDate.now().format(formatter);

        redisTemplate.opsForHyperLogLog().add("uv:" + today, myUserId); // uv
        redisTemplate.opsForHyperLogLog().add("pv:" + today, myUserId + "_" + System.currentTimeMillis()); // pv
        redisTemplate.opsForValue().setBit("dau:" + today, user.getId(), true); // dau

        filterChain.doFilter(request, response);
    }
}
