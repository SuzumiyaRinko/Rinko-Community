package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import suzumiya.model.dto.CommentInsertDTO;
import suzumiya.model.dto.CommentSelectDTO;
import suzumiya.model.pojo.Comment;

public interface ICommentService extends IService<Comment> {

    /* 评论 */
    Long comment(CommentInsertDTO commentInsertDTO);

    /* 删除评论 */
    void delete(Long commentId);

    /* 删除评论 */
    PageInfo<Comment> select(CommentSelectDTO commentSelectDTO);

    /* 点赞或取消点赞 */
    void like(Long commentId);

    /* 判断是否已点赞 */
    Boolean hasLike(Long postId);
}
