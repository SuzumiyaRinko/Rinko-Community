package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentInsertDTO implements Serializable {

    // 评论类型 1:评论post 2:评论别人的comment
    private Integer type;
    // 对象ID（postId或commentId）
    private Long targetId;
    // 内容
    private String content;
    // 图片
    private String[] picturesSplit;

    /* Comment */
    public static final int COMMENT_TYPE_2POST = 1; // 评论post的comment
    public static final int COMMENT_TYPE_2COMMENT = 2; // 评论comment的comment
}