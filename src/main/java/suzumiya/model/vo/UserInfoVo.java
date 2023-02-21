package suzumiya.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoVo implements Serializable {

    private Long id;
    private String nickname;
    private Integer gender;
    private Boolean isFamous;
    private String avatar;

    private Long followingsCount;
    private Long followersCount;
}
