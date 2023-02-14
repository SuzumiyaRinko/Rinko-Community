package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.Message;

public interface IMessageService extends IService<Message> {

    /* 发信息 */
    void sendMessage(MessageInsertDTO messageInsertDTO);
}
