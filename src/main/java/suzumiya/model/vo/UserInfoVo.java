package suzumiya.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserInfoVo implements Serializable {

    private Long id;
    private String nickname;
    private Integer gender;
    private String avatar;

    private List<Integer> roles;

    private Long followingsCount;
    private Long followersCount;
}
