package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.dto.MessageSelectDTO;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.MessageSelectVO;
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
        messageInsertDTO.setMyId(user.getId());
        messageInsertDTO.setFromUserId(user.getId());

        messageService.saveMessage(messageInsertDTO);
        return ResponseGenerator.returnOK("消息发送成功", null);
    }

    @GetMapping("/notReadCount")
    public BaseResponse<Long> notReadCount(@RequestParam("isSystem") Boolean isSystem) {
        // 获取当前用户id
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = user.getId();

        long notReadCount = messageService.notReadCount(isSystem, myId);
        return ResponseGenerator.returnOK("未读消息条数查询成功", notReadCount);
    }

    @GetMapping
    public BaseResponse<MessageSelectVO> getMessages(MessageSelectDTO messageSelectDTO) {
        MessageSelectVO messages;
        if (messageSelectDTO.getTargetId() == null) {
            messages = messageService.getMessages(messageSelectDTO);
        } else {
            messages = messageService.getChatMessages(messageSelectDTO);
        }
        return ResponseGenerator.returnOK("消息查询成功", messages);
    }
}
