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
    Integer getTargetIdByCommentId(@Param("commentId") Long commentId);

    @Delete("UPDATE tb_comment SET is_delete = 1 WHERE target_type = #{targetType} AND target_id = #{targetId}")
    void deleteCommentByTargetTypeAndTargetId(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);
}
