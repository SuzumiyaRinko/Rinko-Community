package suzumiya.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JacksonConfig jacksonConfig;

    /* 配置GET请求的反序列化器 */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(jacksonConfig.new String2DateConverter()); // String -> Date
        registry.addConverter(jacksonConfig.new String2LocalDateConverter()); // String -> LocalDate
        registry.addConverter(jacksonConfig.new String2LocalTimeConverter()); // String -> LocalTime
        registry.addConverter(jacksonConfig.new String2LocalDateTimeConverter()); // String -> LocalDateTime
    }

    /* 允许CORS */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")    // 允许跨域访问的路径
                .allowedOriginPatterns("*")    // 允许跨域访问的域名
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")    // 允许请求方法
                .maxAge(168000)    // 预检间隔时间
                .allowedHeaders("*")  // 允许头部设置
                .allowCredentials(true);    // 是否发送cookie
    }
}
