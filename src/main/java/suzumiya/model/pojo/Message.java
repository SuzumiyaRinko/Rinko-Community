package suzumiya.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("tb_message")
@Data
public class Message {

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
    // postId（可以为null）
    private Long postId;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
    // 是否逻辑删除
    @TableLogic
    private Boolean isDelete;

    // 评论的用户的部分信息（id, nickname, avatar）
    @TableField(exist = false)
    public User fromUser;
}
