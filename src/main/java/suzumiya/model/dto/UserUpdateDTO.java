package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {

    private String nickname;
    private Integer gender;
}
