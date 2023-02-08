package suzumiya.util;

import cn.hutool.http.HttpStatus;
import suzumiya.model.vo.BaseResponse;

public class ResponseGenerator {

    public static <T> BaseResponse<T> returnOK(String message, T data) {
        return new BaseResponse<>(HttpStatus.HTTP_OK, message, data);
    }

    public static <T> BaseResponse<T> returnError(Integer code, String message) {
        return new BaseResponse<>(code, message, null);
    }

//    // 发送请求时 Jwt验证失败
//    public static final BaseResponse<Object> AUTHORIZATION_UNSUCCESSFUL = new BaseResponse<>("10001", "身份认证失败", null);
//    // 发送请求时 当前用户信息在Redis中已过期
//    public static final BaseResponse<Object> REDIS_EXPIRE = new BaseResponse<>("10002", "当前用户信息在Redis中已过期", null);
//    // 登录时 用户名或密码错误
//    public static final BaseResponse<Object> WRONG_AT_USERNAME_PASSWORD = new BaseResponse<>("10003", "用户名或密码错误", null);
//    // 注册时 当前用户名已存在
//    public static final BaseResponse<Object> USER_HAS_BEEN_EXISTED = new BaseResponse<>("10004", "当前用户名已存在", null);
//    // 文件上传时 出现错误
//    public static final BaseResponse<Object> FILES_UPLOAD_ERROR = new BaseResponse<>("10005", "文件上传出现错误", null);
//
//    public static <T> BaseResponse<T> returnOK(String message) {
//        return new BaseResponse<>("200", message, null);
//    }
//
//    public static <T> BaseResponse<T> returnOK(String message, T data) {
//        return new BaseResponse<>("200", message, data);
//    }
//
//    public static BaseResponse<Object> returnError(String code, String message) {
//        return new BaseResponse<>(code, message, null);
//    }


}
