package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import suzumiya.model.pojo.Comment;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    List<String> getFirst3CommentsByTargetId(@Param("targetId") Long targetId);

    @Select("SELECT target_type FROM tb_comment WHERE id = #{commentId}")
    Integer getTargetTypeByCommentId(@Param("commentId") Long commentId);

    @Select("SELECT target_id FROM tb_comment WHERE id = #{commentId}")
    Long getTargetIdByCommentId(@Param("commentId") Long commentId);

    @Select("SELECT user_id FROM tb_comment WHERE id = #{commentId}")
    Long getUserIdByCommentId(@Param("commentId") Long commentId);

    @Select("SELECT content FROM tb_comment WHERE id = #{commentId}")
    String getContentByCommentId(@Param("commentId") Long commentId);

    @Select("SELECT pictures FROM tb_comment WHERE id = #{commentId}")
    String getPicturesByCommentId(@Param("commentId") Long commentId);

    @Delete("UPDATE tb_comment SET is_delete = 1 WHERE target_type = #{targetType} AND target_id = #{targetId}")
    void deleteCommentByTargetTypeAndTargetId(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    @Select("SELECT id FROM tb_comment WHERE target_type = 1 AND target_id = #{postId} AND is_delete = 0")
    List<Long> getAllCommentIdByPostId(@Param("postId") Long postId);

    @Select("SELECT id FROM tb_comment WHERE target_type = 2 AND target_id = #{commentId} AND is_delete = 0")
    List<Long> getAllRecommentIdByCommentId(@Param("commentId") Long commentId);
}
