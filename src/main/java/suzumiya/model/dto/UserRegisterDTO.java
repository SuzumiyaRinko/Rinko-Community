package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    private String username;
    private String password;
    private String confirmPassword;
    private String code; // 用户输入的验证码
    private String uuid; // 该用户的唯一标识
}
