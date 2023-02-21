package suzumiya.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import suzumiya.filter.JwtFilter;
import suzumiya.filter.StatisticsFilter;

@EnableWebSecurity // 标识为Security配置类
@EnableGlobalMethodSecurity(prePostEnabled = true) // 开启SpringSecurity权限控制
public class SecurityConfig {

    @Bean("authenticationManager")
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean("passwordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private StatisticsFilter statisticsFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable() // 关闭csrf
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 不通过Session获取SecurityContext（前后端分离的情况下就要设置这个）
                .and()
                .authorizeRequests(authorize -> authorize
//                        .antMatchers("/test/**").hasAnyAuthority(SecurityConst.SYS_BOOK)
                        .antMatchers("/verifyCode").permitAll()
                        .antMatchers("/user/login").anonymous()
                        .antMatchers("/user/register").anonymous()
                        .antMatchers("/user/activation").anonymous()
                        .anyRequest().authenticated()) // 登录用户和匿名用户都可以访问
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(statisticsFilter, FilterSecurityInterceptor.class)
                .cors(); // 允许跨域访问
        return http.build();
    }
}
