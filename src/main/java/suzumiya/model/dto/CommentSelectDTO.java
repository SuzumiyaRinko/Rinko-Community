package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentSelectDTO implements Serializable {

    private Integer targetType;
    private Long targetId;
    private Integer selectType = 1;
    private int sortType = 1;
    private int pageNum = 1;

    // target类型 对post的评论 2: 对comment的评论
    public static final int TARGET_TYPE_POST = 1;
    public static final int TARGET_TYPE_COMMENT = 2;
    // 1:查看全部 2:只看楼主（在targetType==1的情况下）
    public static final int SELECT_TYPE_DEFAULT = 1;
    public static final int SELECT_TYPE_POSTER = 2;
    // 1:按时间从旧到新 2:按时间从新到旧（在targetType==1的情况下）
    public static final int SORT_TYPE_DEFAULT = 1;
    public static final int SORT_TYPE_REVERSE = 2;
}