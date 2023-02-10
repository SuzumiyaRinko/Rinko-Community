package suzumiya.constant;

public class MQConstant {

    /* 延时队列 */
    public static final String DELAY_DIRECT = "delay.direct";
    // 监听用户激活时间是否结束
    public static final String ACTIVATION_QUEUE = "activation.queue";
    public static final String ACTIVATION_KEY = "activation";

    /* 服务层MQ */
    public static final String SERVICE_DIRECT = "service.direct";
    // 监听用户注册接口
    public static final String USER_REGISTER_QUEUE = "user.register.queue";
    public static final String USER_REGISTER_KEY = "user.register";
    // 监听Post新增接口
    public static final String POST_INSERT_QUEUE = "post.insert.queue";
    public static final String POST_INSERT_KEY = "post.insert";
    // 监听Post删除接口
    public static final String POST_DELETE_QUEUE = "post.delete.queue";
    public static final String POST_DELETE_KEY = "post.delete";
    // 监听Post更新接口
    public static final String POST_UPDATE_QUEUE = "post.update.queue";
    public static final String POST_UPDATE_KEY = "post.update";
}
