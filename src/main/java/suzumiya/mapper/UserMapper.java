package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import suzumiya.model.pojo.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User getUserById(@Param("userId") Long userId);

    List<String> getAuthoritiesStrByUserId(@Param("userId") Long userId);

    @Select("SELECT is_famous FROM sys_user WHERE id = #{userId} AND is_delete = 0")
    Boolean getIsFamousByUserId(@Param("userId") Long userId);

    User getSimpleUserById(@Param("userId") Long userId);

    List<User> getFollowings(@Param("followingIds") List<Long> followingIds);

    @Select("SELECT id,nickname,is_famous,avatar FROM sys_user WHERE is_delete = 0")
    List<User> getSimpleUsers();

    @Delete("DELETE FROM sys_user WHERE is_delete = 1")
    void tableLogicDataClear();
}
