package suzumiya.model.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@TableName("tb_post")
@Document(indexName = "post")
@Data
public class Post {
    // 主键
    @TableId(type = IdType.AUTO)
    @Id
    private Integer id;
    // 发帖的用户id
    @Field(type = FieldType.Long)
    private Long userId;
    // 标题
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    // Tag标签（最大值是2^31-1，所以可以表示31个tag）
    @Field(type = FieldType.Integer)
    private Integer tags;
    // 内容
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    // 是否置顶 0:否 1:是
    @Field(type = FieldType.Integer)
    private Integer isTop;
    // 用于做热度排序的分数
    @Field(type = FieldType.Double)
    private Double score;
    // 创建时间
    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    // 修改时间
    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
    // 是否逻辑删除
    @TableLogic
    @Field(type = FieldType.Integer)
    private Integer isDelete;

    // 获取完整的tag名字
    @TableField(exist = false)
    private String[] tagsStr;
}
