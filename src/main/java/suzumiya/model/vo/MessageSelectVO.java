package suzumiya.model.vo;

import lombok.Data;
import suzumiya.model.pojo.Message;

import java.util.List;

@Data
public class MessageSelectVO {

    // 查询结果
    private Message lastMessage4Public;
    private List<Message> messages;
    // 本次查询的最小messageId(在查询chatMessages时使用)
    private Long lastId;
    // 是否已经没有更多message了
    private Boolean isFinished = false;
}
