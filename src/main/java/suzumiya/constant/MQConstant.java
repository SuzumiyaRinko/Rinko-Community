package suzumiya.constant;

public class MQConstant {

    /* 延时队列 */
    public static final String DELAY_DIRECT = "delay.direct";
    // 监听用户激活时间是否结束
    public static final String ACTIVATION_QUEUE = "activation.queue";
    public static final String ACTIVATION_KEY = "activation";

    /* 用户注册 */
    public static final String SERVICE_DIRECT = "service.direct";
    // 监听用户注册接口
    public static final String USER_REGISTER_QUEUE = "user.register.queue";
    public static final String USER_REGISTER_KEY = "user.register";
}
