package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageInsertDTO implements Serializable {

    // 我的Id
    private Long myId;
    // 发送方的userId
    private Long fromUserId;
    // 接收方的userId
    private Long toUserId;
    // 内容（长度不超过2000）
    private String content;
    // 是否为系统消息
    private Boolean isSystem = false;

    // 触发事件的人
    private Long eventUserId;
    // targetId（可以为null）
    private Long targetId;
    /* 系统消息类型 */
    private int SystemMsgType;
}