package suzumiya.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConstant {

    public static String clusterNode;

    @Value("${commons.cluster-node}")
    public void setClusterNode(String clusterNode) {
        MQConstant.clusterNode = clusterNode;
    }

    /* 延时队列 */
    public static final String DELAY_DIRECT = "delay.direct";
    // 监听用户激活时间是否结束
    public static final String ACTIVATION_QUEUE = "activation.queue";
    public static final String ACTIVATION_KEY = "activation";

    /* 服务层交换机 */
    public static final String SERVICE_DIRECT = "service.direct";

    /* User */
    // 监听用户注册接口
    public static final String USER_REGISTER_QUEUE = "user.register.queue";
    public static final String USER_REGISTER_KEY = "user.register";
    // 监听用户unfollow接口
    public static final String USER_UNFOLLOW_QUEUE = "user.unfollow.queue";
    public static final String USER_UNFOLLOW_KEY = "user.unfollow";

    /* Post */
    // 监听Post新增接口
    public static final String POST_INSERT_QUEUE = "post.insert.queue";
    public static final String POST_INSERT_KEY = "post.insert";
    // 监听Post删除接口
    public static final String POST_DELETE_QUEUE = "post.delete.queue";
    public static final String POST_DELETE_KEY = "post.delete";
    // 监听Post更新接口
    public static final String POST_UPDATE_QUEUE = "post.update.queue";
    public static final String POST_UPDATE_KEY = "post.update";

    /* Cache */
    // 更新缓存
    public static final String CACHE_UPDATE_QUEUE = "cache.update.queue";
    public static final String CACHE_UPDATE_KEY = "cache.update";
    // 缓存交换机
    public static final String CACHE_CLEAR_FANOUT = "cache.clear.fanout";
    // 删除缓存
    public static final String CACHE_CLEAR_QUEUE = "cache.clear.queue." + clusterNode;
    public static final String CACHE_CLEAR_KEY = "cache.clear." + clusterNode;

    /* Message */
    // 发送系统消息
    public static final String MESSAGE_INSERT_QUEUE = "message.insert.queue";
    public static final String MESSAGE_INSERT_KEY = "message.insert";
    // 公共聊天室未读数量
    public static final String MESSAGE_PUBLIC_UNREAD_QUEUE = "message.public.unread.queue";
    public static final String MESSAGE_PUBLIC_UNREAD_KEY = "message.public.unread";
    // ws交换机
    public static final String WS_FANOUT = "ws.fanout";
    // 发送ws消息
    public static final String WS_QUEUE = "ws.queue." + clusterNode;
    public static final String WS_KEY = "ws." + clusterNode;
}
