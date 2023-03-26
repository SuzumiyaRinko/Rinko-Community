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

    List<User> getFollowings(@Param("followingIds") List<Long> followingIds);

    @Select("SELECT id,nickname,gender,avatar FROM sys_user WHERE id = #{userId} AND is_delete = 0")
    User getSimpleUserById(@Param("userId") Long userId);

    @Select("SELECT id,nickname,gender,avatar FROM sys_user WHERE is_delete = 0")
    List<User> getSimpleUsers();

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Integer> getRolesByUserId(@Param("userId") Long userId);

    void setRoles4UserId(@Param("userId") Long userId, @Param("roles") List<Integer> roles);

    @Select("SELECT id FROM sys_user WHERE is_delete = 0")
    List<Long> getAllUserId();

    @Delete("DELETE FROM sys_user WHERE is_delete = 1")
    void tableLogicDataClear();
}
