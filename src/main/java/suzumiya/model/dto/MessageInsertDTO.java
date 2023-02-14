package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageInsertDTO implements Serializable {

    // 发送方的userId
    private Long fromUserId;
    // 接收方的userId
    private Long toUserId;
    // 内容（长度不超过2000）
    private String content;
    // 是否为系统消息
    private Boolean isSystem = false;

    // postId（可以为null）
    private Long postId;

    private int SystemMsgType;
    public static final int SYSTEM_TYPE_LIKE = 1;
    public static final int SYSTEM_TYPE_COMMENT = 2;
    public static final int SYSTEM_TYPE_COLLECT = 3;
}