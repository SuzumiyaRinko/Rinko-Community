package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

/*
    例子：messageType == 1, targetId == 1, 表示删除id=1的系统消息
         messageType == 1, isAll == true, 表示删除当前用户的所有系统消息
         messageType == 2, targetId == 1, 表示删除from_user_id=1的私聊列表（暂时不显示）, 并把未读消息数设置为0
         messageType == 2, isAll == true, 表示删除当前用户的所有私聊列表（暂时不显示）, 并把未读消息数设置为0
*/
@Data
public class MessageDeleteDTO implements Serializable {

    private Integer messageType;
    private Long targetId;
    private Boolean isAll; // 是否设置所有消息未"已读"
}
