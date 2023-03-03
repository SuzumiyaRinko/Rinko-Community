package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUnfollowDTO implements Serializable {

    private Long myUserId;
    private Long targetId;
}
