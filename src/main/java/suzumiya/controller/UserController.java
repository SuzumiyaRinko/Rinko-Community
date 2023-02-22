package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import suzumiya.model.dto.UserLoginDTO;
import suzumiya.model.dto.UserRegisterDTO;
import suzumiya.model.dto.UserUpdateDTO;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.FollowingSelectVO;
import suzumiya.model.vo.UserInfoVo;
import suzumiya.service.IFileService;
import suzumiya.service.IUserService;
import suzumiya.util.ResponseGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IFileService fileService;

    @PostMapping("/login")
    public BaseResponse<String> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        /* 用户登录 */
        String token = userService.login(userLoginDTO, request);
        return ResponseGenerator.returnOK("用户登录成功", token);
    }

    @PostMapping("/logout")
    public BaseResponse<String> logout() {
        /* 用户登出 */
        userService.logout();
        return ResponseGenerator.returnOK("用户退出登录成功", null);
    }

    @PostMapping("/register")
    public BaseResponse<String> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        /* 用户注册 */
        userService.register(userRegisterDTO);
        return ResponseGenerator.returnOK("用户注册成功", null);
    }

    @PostMapping("/activation/{uuid}")
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

    @GetMapping("/userInfo")
    public BaseResponse<UserInfoVo> getUserInfo(@RequestParam(value = "userId", required = false) Long userId) {
        UserInfoVo userInfo = userService.getUserInfo(userId);
        return ResponseGenerator.returnOK("成功查询用户基础信息", userInfo);
    }

    @PostMapping("/uploadAvatar")
    public BaseResponse<String> uploadAvatar(MultipartFile file) throws IOException {
        String path = fileService.uploadAvatar(file);
        return ResponseGenerator.returnOK("成功上传文件", path);
    }

    @PostMapping("/updateUserInfo")
    public BaseResponse<Object> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) throws IOException {
        userService.updateUserInfo(userUpdateDTO);
        return ResponseGenerator.returnOK("用户信息修改成功", null);
    }
}
