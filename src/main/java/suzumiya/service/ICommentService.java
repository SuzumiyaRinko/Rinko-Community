package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageInfo;
import suzumiya.model.dto.CommentInsertDTO;
import suzumiya.model.dto.CommentSelectDTO;
import suzumiya.model.pojo.Comment;

public interface ICommentService extends IService<Comment> {

    /* 评论 */
    Long comment(CommentInsertDTO commentInsertDTO) throws JsonProcessingException;

    /* 删除评论 */
    void delete(Long commentId);

    /* 分页查询评论 */
    PageInfo<Comment> select(CommentSelectDTO commentSelectDTO);

    /* 根据commentId查询comment */
    Comment getCommentByCommentId(Long commentId);

    /* 点赞或取消点赞 */
    void like(Long commentId);

    /* 判断是否已点赞 */
    Boolean hasLike(Long postId);
}
