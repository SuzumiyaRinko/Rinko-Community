package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import suzumiya.model.dto.UserRegisterDTO;
import suzumiya.model.pojo.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IUserService extends IService<User> {

    /* 用户登录 */
    String login(User user, HttpServletRequest request);

    /* 用户登出 */
    void logout();

    /* 用户注册 */
    void register(UserRegisterDTO userRegisterDTO);

    /* 用户激活 */
    void activate(String uuid, HttpServletResponse response) throws IOException;

    /* 获取用户的简单信息（多级缓存） */
    User getSimpleUserById(Long userId);
}
