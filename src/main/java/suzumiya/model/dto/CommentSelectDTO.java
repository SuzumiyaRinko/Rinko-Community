package suzumiya.model.dto;

import lombok.Data;

@Data
public class CommentSelectDTO {

    private Integer targetType = 1; // post的评论 或 comment的评论
    private Long targetId; // postId或commentId
    private Integer selectType = 1; // 1:查看全部 2:只看楼主（在targetType==1的情况下）
    private int sortType = 1; // 1:按时间从旧到新 2:按时间从新到旧（在targetType==1的情况下）
    private int pageNum = 1;

    public static final int TARGET_TYPE_POST = 1;
    public static final int TARGET_TYPE_COMMENT = 2;
    public static final int SELECT_TYPE_DEFAULT = 1;
    public static final int SELECT_TYPE_POSTER = 2;
    public static final int SORT_TYPE_DEFAULT = 1;
    public static final int SORT_TYPE_REVERSE = 2;
}