package suzumiya.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean("kaptcha")
    public DefaultKaptcha kaptcha() {

        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border.thickness", "2"); // 边框宽度
        properties.setProperty("kaptcha.textproducer.font.color", "black"); // 文字颜色
        properties.setProperty("kaptcha.image.width", "200"); // 宽
        properties.setProperty("kaptcha.image.height", "40"); // 高
        properties.setProperty("kaptcha.textproducer.char.string", "23456789abcdefghkmnpqrstuvwxyzABCDEFGHKMNPRSTUVWXYZ"); // 有效字符
        properties.setProperty("kaptcha.textproducer.font.size", "30"); // 字体大小
        properties.setProperty("kaptcha.textproducer.char.space", "20"); // 字体间距
        properties.setProperty("kaptcha.textproducer.char.length", "4"); // 验证码长度
        properties.setProperty("kaptcha.textproducer.font.names", "微软雅黑"); // 字体
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise"); // 去除干扰线
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);

        return defaultKaptcha;
    }
}
