package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.dto.MessageSelectDTO;
import suzumiya.model.pojo.Message;
import suzumiya.model.vo.MessageSelectVO;

public interface IMessageService extends IService<Message> {

    /* 发信息 */
    void sendMessage(MessageInsertDTO messageInsertDTO);

    /* 查询未读消息条数 */
    long notReadCount(Boolean isSystem, Long myId);

    /* 查询系统消息或对话列表 */
    // 如果查询的是对话列表，那么获得的是每个对话对象的第一条消息
    MessageSelectVO getMessages(MessageSelectDTO messageSelectDTO);

    /* 查询对话消息 */
    MessageSelectVO getChatMessages(MessageSelectDTO messageSelectDTO);
}
