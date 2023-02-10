package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import suzumiya.model.pojo.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User getUserById(@Param("userId") Long userId);

    List<String> getAuthoritiesStrByUserId(@Param("userId") Long userId);

    @Select("SELECT is_famous FROM sys_user WHERE id = #{userId}")
    Boolean getIsFamousByUserId(@Param("userId") Long userId);
}
