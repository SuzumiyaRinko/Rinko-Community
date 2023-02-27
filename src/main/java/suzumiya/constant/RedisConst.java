package suzumiya.constant;

public class RedisConst {

    /* User */
    // 某个用户的访问API的频率
    public static final String USER_FREQUENCY_KEY = "user:frequency:"; // user:frequency:
    // 某个用户是否被ban掉（30s）
    public static final String USER_BAN_KEY = "user:ban:"; // user:ban:{ip}
    // 密码错误的重试次数
    public static final String LOGIN_RETRY_USER_KEY = "login:retry:user:"; // login:retry:user:{ip}
    // 已经登录了的用户的用户信息
    public static final String LOGIN_USER_KEY = "login:user:"; // login:retry:user:{userId}
    // 需要在30mins内激活账号的userId
    public static final String ACTIVATION_USER_KEY = "activate:user:"; // activate:user:{activationUUID}
    // 某IP 24小时内的注册次数
    public static final String REGISTER_TIMES_KEY = "registerTimes:"; // registerTimes:{ip}
    // 某个user的collection的zset集合
    public static final String USER_COLLECTIONS_KEY = "user:collections:"; // user:collections:{userId}
    // 某个user的following的set集合
    public static final String USER_FOLLOWING_KEY = "user:following:"; // user:following:{userId}
    // 某个user的follower的set集合
    public static final String USER_FOLLOWER_KEY = "user:follower:"; // user:follower:{userId}
    // 某个user的私信对象的set集合
    public static final String USER_MESSAGE_KEY = "user:message:"; // user:message:{userId}

    /* Post */
    // 当前post总数
    public static final String POST_TOTAL_KEY = "post:total";
    // 要刷新分数的post集合
    public static final String POST_SCORE_UPDATE_KEY = "post:scoreUpdate";
    // 某个post的like数
    public static final String POST_LIKE_COUNT_KEY = "post:like:count:"; // post:like:count:{postId}
    // 某个post的like用户的set集合
    public static final String POST_LIKE_LIST_KEY = "post:like:list:"; // post:like:list:{postId}
    // 某个post的comment数
    public static final String POST_COMMENT_COUNT_KEY = "post:comment:count:"; // post:comment:count:{postId}
    // 某个post的collection数
    public static final String POST_COLLECTION_COUNT_KEY = "post:collection:count:"; // post:collection:count:{postId}
    // 某个post的collection用户的set集合
    public static final String POST_COLLECTION_LIST_KEY = "post:collection:list:"; // post:collection:list:{postId}

    /* Comment */
    // 某个comment的like数
    public static final String COMMENT_LIKE_COUNT_KEY = "comment:like:count:"; // comment:like:count:{commentId}
    // 某个comment的like用户的set集合
    public static final String COMMENT_LIKE_LIST_KEY = "comment:like:list:"; // comment:like:list:{commentId}
    // 某个comment的recomment数
    public static final String COMMENT_RECOMMENT_COUNT_KEY = "comment:recomment:count:"; // comment:recomment:count:{commentId}
}
