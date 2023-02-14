package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.BaseResponse;
import suzumiya.service.IMessageService;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    @PostMapping
    public BaseResponse<Object> sendMessage(@RequestBody MessageInsertDTO messageInsertDTO) {
        // 获取当前用户id
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        messageInsertDTO.setFromUserId(user.getId());

        messageService.sendMessage(messageInsertDTO);
        return ResponseGenerator.returnOK("消息发送成功", null);
    }
}
