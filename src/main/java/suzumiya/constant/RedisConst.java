package suzumiya.constant;

public class RedisConst {

    // 密码错误的重试次数
    public static final String LOGIN_RETRY_USER_KEY = "login:retry:user:";
    // 已经登录了的用户的用户信息
    public static final String LOGIN_USER_KEY = "login:user:";
    // 需要在30mins内激活账号的userId
    public static final String ACTIVATION_USER_KEY = "activation:user:";
    // 某IP 24小时内的注册次数
    public static final String REGISTER_TIMES_KEY = "registerTimes:";
}
