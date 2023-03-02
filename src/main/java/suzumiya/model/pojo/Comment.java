package suzumiya.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@TableName("tb_comment")
@Data
public class Comment implements Serializable {

    // 主键
    @TableId(type = IdType.AUTO)
    private Long id;
    // 评论的用户的userId
    private Long userId;
    // 评论类型 1:评论post 2:评论别人的comment
    private Integer targetType;
    // 对象ID（postId或commentId）
    private Long targetId;
    // 内容（长度不超过2000）
    private String content;
    // 图片
    @Field(type = FieldType.Text, index = false)
    private String pictures;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
    // 是否逻辑删除
    @TableLogic
    private Boolean isDelete;

    // 评论的用户的部分信息（id, nickname, gender, is_famous, avatar）
    @TableField(exist = false)
    public User commentUser;
    // 某个comment的前三条简单comment（userNickname, content）
    @TableField(exist = false)
    public List<String> first3Comments;
    // 点赞数量
    @TableField(exist = false)
    public Integer likeCount;
    // 评论数量
    @TableField(exist = false)
    public Integer commentCount;

    // 图片
    @TableField(exist = false)
    public String[] picturesSplit;
}
