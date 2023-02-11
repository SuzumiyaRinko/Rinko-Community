package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import suzumiya.model.pojo.Post;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    List<Post> getPostsForUpdatingScore(@Param("postIds") List<Integer> postIds);

    @Delete("DELETE FROM tb_post WHERE is_delete = 1")
    void tableLogicDataClear();
}
