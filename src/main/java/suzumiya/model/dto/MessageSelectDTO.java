package suzumiya.model.dto;

import lombok.Data;

@Data
public class MessageSelectDTO {

    private Boolean isSystem; // 查询系统消息或对话列表
    private Long targetId; // 对方的userId（查询对话消息时需要传参）
    private Long lastId; // 按照ID降序查询（时间降序）
}