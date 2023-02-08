package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import suzumiya.model.pojo.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User getUserById(@Param("userId") Long userId);

    List<String> getAuthoritiesStrByUserId(@Param("userId") Long userId);
}
