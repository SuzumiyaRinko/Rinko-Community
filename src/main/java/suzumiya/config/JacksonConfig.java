package suzumiya.config;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Configuration("JacksonConfig")
@Slf4j
public class JacksonConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        // 使Jackson支持Java8时间API
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        objectMapper.registerModule(new JavaTimeModule());
    }

    /* 定义GET请求 String->Date 反序列化器 */
    class String2DateConverter implements Converter<String, Date> {

        private final SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private final SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public Date convert(String source) {
            /* 判空 */
            if (StrUtil.isBlank(source)) {
                return null;
            }
            /* 解析 */
            Date result;
            try {
                result = formatter1.parse(source); // 第一种类型
            } catch (Exception ex) {
                try {
                    result = formatter2.parse(source); // 如果报错了说明是第二种情况
                } catch (ParseException e) {
                    e.printStackTrace(); // 如果报错了说明是程序问题，请及时debug
                    return null;
                }
            }
            return result;
        }
    }

    /* 定义GET请求 String->LocalDate 反序列化器 */
    class String2LocalDateConverter implements Converter<String, LocalDate> {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public LocalDate convert(String source) {
            /* 判空 */
            if (StrUtil.isBlank(source)) {
                return null;
            }
            /* 解析 */
            return LocalDate.parse(source);
        }
    }

    /* 定义GET请求 String->LocalTime 反序列化器 */
    class String2LocalTimeConverter implements Converter<String, LocalTime> {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public LocalTime convert(String source) {
            /* 判空 */
            if (StrUtil.isBlank(source)) {
                return null;
            }
            /* 解析 */
            return LocalTime.parse(source);
        }
    }

    /* 定义GET请求 String->LocalDateTime 反序列化器 */
    class String2LocalDateTimeConverter implements Converter<String, LocalDateTime> {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public LocalDateTime convert(String source) {
            /* 判空 */
            if (StrUtil.isBlank(source)) {
                return null;
            }
            /* 解析 */
            return LocalDateTime.parse(source, formatter);
        }
    }
}
