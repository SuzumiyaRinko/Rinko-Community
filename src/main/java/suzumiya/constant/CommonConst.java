package suzumiya.constant;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CommonConst {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /* 服务层 */
    public static final String PREFIX_ACTIVATION_URL = "http://localhost:8081/user/activation/";
    public static final String USER_LOGIN_URL = "#";
    public static final String USER_REGISTER_URL = "#";
    public static final String MAIL_FROM = "Txz2018911711@163.com";
    public static final int STANDARD_PAGE_SIZE = 10;

    /* 普通常量 */
    public static final String PREFIX_BASE64IMG = "data:image/jpeg;base64,";
    public static final long COMMUNITY_EPOCH = LocalDateTime.parse("2023-02-11 00:00:00", formatter).toEpochSecond(ZoneOffset.of("+8"));

    /* 正则表达式 */
    public static final String REGEX_PASSWORD = "[0-9a-zA-Z]{8,16}";
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /* 网页 */
    public static final String HTML_ACTIVATION = "<html>\n" +
            "<body>\n" +
            "\t<h2 style=\"text-align: center; font-weight: 700; font-size: 42px; margin-top: 10px\">欢迎用户</h2>\n" +
            "\t<h2 style=\"text-decoration: none; color: black; text-align: center; font-weight: 500; font-size: 30px;\"> <xxxxx> </h2>\n" +
            "\t<h2 style=\"text-align: center; font-weight: 700; font-size: 42px; margin-top: 10px\">使用Rinko-Community</h2>\n" +
            "\t<div style=\"text-align: center\">\n" +
            "\t\t<a class=\"font\" style=\"text-decoration: none; font-weight: 400; font-size: 24px; color: red;\" href='<yyyyy>'><点击此处跳转到账号激活页面></a>\n" +
            "\t</div>\n" +
            "</body></html>";
    public static final String HTML_ACTIVATION_SUCCESS = "<html>\n" +
            "<title>Rinko-Community | 账号激活</title>\n" +
            "<body>\n" +
            "\t<h2 style=\"text-align: center; font-weight: 700; font-size: 42px; margin-top: 10px\">欢迎用户</h2>\n" +
            "\t<h2 style=\"text-decoration: none; color: black; text-align: center; font-weight: 500; font-size: 30px;\"> <xxxxx> </h2>\n" +
            "\t<h2 style=\"text-align: center; font-weight: 700; font-size: 42px; margin-top: 10px\">使用Rinko-Community</h2>\n" +
            "\t<h2 style=\"text-align: center; font-weight: 400; font-size: 24px; color: red; margin-top: -10px;\">该账号已激活！</h2>\n" +
            "\t<div style=\"text-align: center\">\n" +
            "\t\t<a class=\"font\" style=\"text-decoration: none; font-weight: 400; font-size: 18px; color: red;\" href='<yyyyy>' target=\"_blank\"><点击此处跳转到账号登录页面></a>\n" +
            "\t</div>\n" +
            "</body>\n" +
            "</html>";
    public static final String HTML_ACTIVATION_EXPIRED = "<html>\n" +
            "<title>Rinko-Community | 账号激活</title>\n" +
            "<body>\n" +
            "\t<h2 style=\"text-align: center; font-weight: 700; font-size: 42px; margin-top: 10px\">欢迎用户使用Rinko-Community</h2>\n" +
            "\t<h2 style=\"text-align: center; font-weight: 400; font-size: 24px; color: red; margin-top: -10px;\">账号激活超时，请重新注册！</h2>\n" +
            "\t<div style=\"text-align: center\">\n" +
            "\t\t<a class=\"font\" style=\"text-decoration: none; font-weight: 400; font-size: 18px; color: red;\" href='<yyyyy>' target=\"_blank\"><点击此处跳转到账号注册页面></a>\n" +
            "\t</div>\n" +
            "</body>\n" +
            "</html>";
}
