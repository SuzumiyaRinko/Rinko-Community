package suzumiya.model.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {

    private String username;
    private String password;
    private String code; // 用户输入的验证码
    private String correctCode; // 后台返回的验证码
}
