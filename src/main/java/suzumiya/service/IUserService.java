package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import suzumiya.model.dto.UserLoginDTO;
import suzumiya.model.dto.UserRegisterDTO;
import suzumiya.model.dto.UserUpdateDTO;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.FollowingSelectVO;
import suzumiya.model.vo.UserInfoVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IUserService extends IService<User> {

    /* 用户登录 */
    String login(UserLoginDTO userLoginDTO, HttpServletRequest request);

    /* 用户登出 */
    void logout();

    /* 用户注册 */
    void register(UserRegisterDTO userRegisterDTO);

    /* 用户激活 */
    void activate(String uuid, HttpServletResponse response) throws IOException;

    /* 获取用户的简单信息（多级缓存） */
    User getSimpleUserById(Long userId);

    /* 关注/取消关注 */
    void follow(Long targetId);

    Boolean hasFollow(Long targetId);

    /* 获取关注列表 */
    int FOLLOWINGS_STANDARD_PAGE_SIZE = 30;

    FollowingSelectVO getFollowings(Long lastId);

    UserInfoVo getUserInfo(Long userId);

    String uploadAvatar(MultipartFile multipartFile) throws IOException;

    void updateUserInfo(UserUpdateDTO userUpdateDTO);
}
