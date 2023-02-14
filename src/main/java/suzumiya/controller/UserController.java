package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.UserRegisterDTO;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.FollowingSelectVO;
import suzumiya.service.IUserService;
import suzumiya.util.ResponseGenerator;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public BaseResponse<String> login(@RequestBody User user, HttpServletRequest request) {
        /* 用户登录 */
        String token = userService.login(user, request);
        return ResponseGenerator.returnOK("用户登录成功", token);
    }

    @PostMapping("/logout")
    public BaseResponse<String> logout() {
        /* 用户登出 */
        userService.logout();
        return ResponseGenerator.returnOK("用户登出成功", null);
    }

    @PostMapping("/register")
    public BaseResponse<String> register(@RequestBody UserRegisterDTO userRegisterDTO) throws MessagingException {
        /* 用户注册 */
        userService.register(userRegisterDTO);
        return ResponseGenerator.returnOK("用户注册成功", null);
    }

    @GetMapping("/activation/{uuid}")
    public void activate(HttpServletResponse response, @PathVariable("uuid") String uuid) throws IOException {
        /* 激活用户（激活页面在Service层返回） */
        userService.activate(uuid, response);
    }

    @PostMapping("/follow/{targetId}")
    public BaseResponse<Object> follow(@PathVariable("targetId") Long targetId) {
        /* 关注 / 取消关注 */
        int result = userService.follow(targetId);
        if (result == IUserService.FOLLOW_SUCCESS) {
            return ResponseGenerator.returnOK("已关注该用户", null);
        } else {
            return ResponseGenerator.returnOK("已取消关注该用户", null);
        }
    }

    @GetMapping("/following")
    public BaseResponse<FollowingSelectVO> getFollowings(@RequestParam(name = "lastId", required = false) Long lastId) {
        /* 获取当前用户的关注列表 */
        FollowingSelectVO followingSelectVO = userService.getFollowings(lastId);
        return ResponseGenerator.returnOK("查询关注列表成功", followingSelectVO);
    }
}
