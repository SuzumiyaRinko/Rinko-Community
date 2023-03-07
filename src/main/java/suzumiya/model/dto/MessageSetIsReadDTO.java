package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

/*
    例子：messageType == 1, targetId == 1, 表示设置id=1的系统消息为"已读"
         messageType == 1, isAll == true, 表示设置当前用户的所有系统消息为"已读"
         messageType == 2, targetId == 1, 表示设置fromUserId=1的unreadCount为0
         messageType == 2, isAll == true, 表示设置当前用户的unreadCount为0
*/
@Data
public class MessageSetIsReadDTO implements Serializable {

    private Integer messageType;
    private Long targetId;
    private Boolean isAll; // 是否设置所有消息未"已读"
}
