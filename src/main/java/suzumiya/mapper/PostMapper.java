package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import suzumiya.model.pojo.Post;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
