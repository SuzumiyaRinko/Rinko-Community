package suzumiya.constant;

public class CacheConst {

    /* type */
    public static final int VALUE_TYPE_SIMPLE = 1;
    public static final int VALUE_TYPE_POJO = 2;

    /* caffeineType */
    public static final int CAFFEINE_TYPE_USER = 1;
    public static final int CAFFEINE_TYPE_POST = 2;

    /* User */
    public static final String CACHE_USER_KEY_PATTERN = "cache:user:*";
    public static final String CACHE_USER_KEY = "cache:user:"; // cache:user:{userId}

    /* Post */
    public static final String CACHE_POST_KEY_PATTERN = "cache:post:*";
    public static final String CACHE_POST_NOT_FAMOUS_KEY = "cache:post:0:"; // cache:post:0:{sortType}:{pageNum}
    public static final String CACHE_POST_FAMOUS_KEY = "cache:post:"; // cache:post:{userId}:0:{pageNum}
}
