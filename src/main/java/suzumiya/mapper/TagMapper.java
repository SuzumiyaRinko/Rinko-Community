package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import suzumiya.model.pojo.Tag;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    List<String> getAllNameByTagIDs(@Param("tagIDs") List<Integer> tagIDs);
}
