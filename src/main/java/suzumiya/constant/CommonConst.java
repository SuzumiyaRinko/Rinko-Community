package suzumiya.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class CommonConst {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /* 服务层 */
    // User
    public static String PREFIX_ACTIVATION_URL;
    public static String USER_LOGIN_URL;
    public static String USER_REGISTER_URL;

    @Value("${url.prefix-activation-url}")
    public void setPREFIX_ACTIVATION_URL(String PREFIX_ACTIVATION_URL) {
        CommonConst.PREFIX_ACTIVATION_URL = PREFIX_ACTIVATION_URL;
    }

    @Value("${url.user-login-url}")
    public void setUSER_LOGIN_URL(String USER_LOGIN_URL) {
        CommonConst.USER_LOGIN_URL = USER_LOGIN_URL;
    }

    @Value("${url.user-register-url}")
    public void setUSER_REGISTER_URL(String USER_REGISTER_URL) {
        CommonConst.USER_REGISTER_URL = USER_REGISTER_URL;
    }

    public static final String MAIL_FROM = "Txz2018911711@163.com";
    public static final String DEFAULT_AVATAR = "/default_avatar.png";
    // Comment
    public static final int COMMENT_TYPE_2POST = 1;
    public static final int COMMENT_TYPE_2COMMENT = 2;

    /* 普通常量 */
    public static final String PREFIX_BASE64IMG = "data:image/jpeg;base64,";
    public static final long COMMUNITY_EPOCH_SECOND = LocalDateTime.parse("2023-02-11 00:00:00", formatter).toEpochSecond(ZoneOffset.of("+8"));
    public static final int STANDARD_PAGE_SIZE = 10;
    public static final String REPLACEMENT_ENTER = "%%br%%";

    /* 正则表达式 */
    public static final String REGEX_PASSWORD = "[0-9a-zA-Z]{8,16}";
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /* 网页 */
    public static String HTML_ACTIVATION = null;
    public static String HTML_ACTIVATION_SUCCESS = null;
    public static String HTML_ACTIVATION_EXPIRED = null;

    static {
        InputStream in1 = null;
        InputStream in2 = null;
        InputStream in3 = null;
        try {
            // HTML_ACTIVATION
            in1 = new ClassPathResource("static/html/html_activation.txt").getInputStream();
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int cnt;
            while ((cnt = in1.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, cnt));
            }
            HTML_ACTIVATION = sb.toString();

            // HTML_ACTIVATION_SUCCESS
            in2 = new ClassPathResource("static/html/html_activation_success.txt").getInputStream();
            sb = new StringBuilder();
            bytes = new byte[1024];
            while ((cnt = in2.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, cnt));
            }
            HTML_ACTIVATION_SUCCESS = sb.toString();

            // HTML_ACTIVATION_EXPIRED
            in3 = new ClassPathResource("static/html/html_activation_expired.txt").getInputStream();
            sb = new StringBuilder();
            bytes = new byte[1024];
            while ((cnt = in3.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, cnt));
            }
            HTML_ACTIVATION_EXPIRED = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in1.close();
                in2.close();
                in3.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
