package suzumiya.model.dto;

import lombok.Data;

@Data
public class CommentInsertDTO {

    // 评论类型 1:评论post 2:评论别人的comment
    private Integer type;
    // 对象ID（postId或commentId）
    private Long targetId;
    // 内容
    private String content;

    /* Comment */
    public static final int COMMENT_TYPE_2POST = 1; // 评论post的comment
    public static final int COMMENT_TYPE_2COMMENT = 2; // 评论comment的comment
}