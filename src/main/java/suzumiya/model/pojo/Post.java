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
    // Tag标签（最大值是2^31-1，所以可以表示31个tag，二进制从左往右读）
    private Integer tags;
    // 内容
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", copyTo = "searchField")
    private String content;
    // 是否置顶 0:否 1:是
    @Field(type = FieldType.Integer)
    private Integer isTop;
    // 用于做热度排序的分数
    @Field(type = FieldType.Double)
    private Double score;
    // 创建时间（必须要写 format = {}）
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
    // 是否逻辑删除
    @TableLogic
    @Field(type = FieldType.Integer)
    private Integer isDelete;

    // tag数组
    @Field(type = FieldType.Integer)
    @TableField(exist = false)
    private List<Integer> tagIDs;
    // 获取完整的tag名字
    @TableField(exist = false)
    private List<String> tagsStr;

    // 用于搜索的字段
    @JsonIgnore
    @TableField(exist = false)
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", ignoreFields = "all", excludeFromSource = true)
    private String searchField;
}
