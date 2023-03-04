package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageSelectDTO implements Serializable {

    private Boolean isSystem; // true:系统消息 false:私信列表
    private Long targetId; // 对方的userId（isSystem==false时需要传参）
    private Long lastId = Long.MAX_VALUE; // 按照ID降序查询（时间降序）
}