package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import suzumiya.model.pojo.Comment;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    List<String> getFirst3CommentsByTargetId(@Param("targetId") Long targetId);
}
