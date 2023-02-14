package suzumiya.model.vo;

import lombok.Data;
import suzumiya.model.pojo.User;

import java.util.List;

@Data
public class FollowingSelectVO {

    // 查询结果
    private List<User> followings;
    // 本次查询的最大userId
    private Long lastId;
}
