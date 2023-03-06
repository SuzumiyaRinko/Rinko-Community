package suzumiya.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("tb_message")
@Data
public class Message implements Serializable {

    // 主键
    @TableId(type = IdType.AUTO)
    private Long id;
    // 发送方的userId（系统消息fromUserId=0）
    private Long fromUserId;
    // 接收方的userId
    private Long toUserId;
    // 内容（长度不超过2000）
    private String content;
    // 是否已读 0:未读 1:已读
    private Boolean isRead;
    // 目标id（可以为null）
    private Long targetId;
    /* 系统消息类型 */
    private Integer systemMsgType;
    public static final int SYSTEM_TYPE_POST_LIKE = 1; // 有人点赞自己的post
    public static final int SYSTEM_TYPE_POST_COMMENT = 2; // 有人评论自己的post
    public static final int SYSTEM_TYPE_POST_COLLECT = 3; // 有人收藏自己的post
    public static final int SYSTEM_TYPE_POST_FOLLOWING = 4; // 自己发post，然后通知关注自己的用户
    public static final int SYSTEM_TYPE_COMMENT_LIKE = 5; // 有人点赞自己的comment
    public static final int SYSTEM_TYPE_COMMENT_RECOMMENT = 6; // 有人评论自己的comment
    public static final int SYSTEM_TYPE_SOMEONE_FOLLOWING = 7; // 有人关注自己
    // 触发事件的人
    private Long eventUserId;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
    // 是否逻辑删除
    @TableLogic
    private Boolean isDelete;

    // 评论的用户的部分信息（id, nickname, avatar, roles）
    @TableField(exist = false)
    public User eventUser;
    // 未读消息条数
    @TableField(exist = false)
    private Integer unreadCount;
}
