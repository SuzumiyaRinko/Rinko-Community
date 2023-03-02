package suzumiya.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@TableName("tb_post")
@Document(indexName = "post")
@Data
public class Post implements Serializable {
    // 主键
    @TableId(type = IdType.AUTO)
    @Id
    private Long id;
    // 发帖的用户id
    @Field(type = FieldType.Long)
    private Long userId;
    // 标题
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", copyTo = "searchField")
    private String title;
    // 内容（长度不能超过5000）
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", copyTo = "searchField")
    private String content;
    // 图片
    @Field(type = FieldType.Text, index = false)
    private String pictures;
    // Tag标签（最大值是2^31-1，所以可以表示31个tag，二进制从左往右读）
    @Field(type = FieldType.Integer, index = false)
    private Integer tags;
    // 是否加精 0:否 1:是
    @Field(type = FieldType.Boolean, index = false)
    private Boolean isWonderful;
    // 是否置顶 0:否 1:是
    @Field(type = FieldType.Boolean)
    private Boolean isTop;
    // 用于做热度排序的分数
    @Field(type = FieldType.Double, index = false)
    private Double score;
    // 创建时间（必须要写 format = {}）
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss", index = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    // 修改时间
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss", index = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
    // 是否逻辑删除
    @Field(type = FieldType.Boolean, index = false)
    @TableLogic
    private Boolean isDelete;

    // tag数组
    @Field(type = FieldType.Integer)
    @TableField(exist = false)
    private List<Integer> tagIDs;
    // 获取完整的tag名字
    @Field(type = FieldType.Keyword, index = false)
    @TableField(exist = false)
    private List<String> tagsStr;

    // 用于搜索的字段
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", ignoreFields = "searchField", excludeFromSource = true)
    @JsonIgnore
    @TableField(exist = false)
    private String searchField;

    // 评论的用户的部分信息（id, nickname, avatar）
    @TableField(exist = false)
    public User postUser;
    // 点赞数量
    @TableField(exist = false)
    public Integer likeCount;
    // 评论数量
    @TableField(exist = false)
    public Integer commentCount;
    // 收藏数量
    @TableField(exist = false)
    public Integer collectionCount;

    // 前三张图片
    @TableField(exist = false)
    public String[] first3PicturesSplit;
    // 图片
    @TableField(exist = false)
    public String[] picturesSplit;
}
