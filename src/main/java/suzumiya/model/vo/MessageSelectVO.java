package suzumiya.model.vo;

import lombok.Data;
import suzumiya.model.pojo.Message;

import java.util.List;

@Data
public class MessageSelectVO {

    // 查询结果
    private List<Message> messages;
    // 本次查询的最小messageId
    private Long lastId;
}
