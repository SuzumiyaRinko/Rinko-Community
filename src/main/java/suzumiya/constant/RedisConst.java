package suzumiya.constant;

public class RedisConst {

    /* User */
    // 密码错误的重试次数
    public static final String LOGIN_RETRY_USER_KEY = "login:retry:user:";
    // 已经登录了的用户的用户信息
    public static final String LOGIN_USER_KEY = "login:user:";
    // 需要在30mins内激活账号的userId
    public static final String ACTIVATION_USER_KEY = "activate:user:";
    // 某IP 24小时内的注册次数
    public static final String REGISTER_TIMES_KEY = "registerTimes:";

    /* Post */
    // 要刷新分数的post集合
    public static final String POST_SCORE_UPDATE_KEY = "post:scoreUpdate";
}
