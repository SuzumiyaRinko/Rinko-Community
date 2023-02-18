package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import suzumiya.model.pojo.Post;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    List<Post> getPostsForUpdatingScore(@Param("postIds") List<Integer> postIds);

    @Select("SELECT user_id FROM tb_post WHERE id = #{postId}")
    Long getUserIdByPostId(@Param("postId") Long postId);

    @Select("SELECT title FROM tb_post WHERE id = #{postId}")
    String getTitleByPostId(@Param("postId") Long postId);

    @Select("SELECT id FROM tb_post WHERE user_id = #{userId} LIMIT #{startIndex}, #{size}")
    List<Long> getPostIdsByUserId(@Param("postId") Long postId, @Param("startIndex") int startIndex, @Param("size") int size);

    @Select("SELECT id,is_wonderful FROM tb_post ORDER BY score DESC LIMIT 0,10")
    List<Post> getTop10PostWithIdAndIsWonderful();

    @Delete("DELETE FROM tb_post WHERE is_delete = 1")
    void tableLogicDataClear();
}
