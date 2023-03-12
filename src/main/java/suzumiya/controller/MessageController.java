package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.MessageDeleteDTO;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.dto.MessageSelectDTO;
import suzumiya.model.dto.MessageSetIsReadDTO;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.MessageSelectVO;
import suzumiya.service.IMessageService;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/message")
@Slf4j
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

    // 获取当前用户私信列表的总未读消息数
    @GetMapping("/notReadCount")
    public BaseResponse<Integer> notReadCount() {
        // 获取当前用户id
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();

        Integer notReadCount = messageService.notReadCount(myUserId);
        return ResponseGenerator.returnOK("未读消息条数查询成功", notReadCount);
    }

    @GetMapping
    public BaseResponse<MessageSelectVO> getMessages(MessageSelectDTO messageSelectDTO) {
        MessageSelectVO messages;
        if (messageSelectDTO.getTargetId() == null) {
            messages = messageService.getMessages(messageSelectDTO);
        } else {
            log.debug("MessageController.getMessages.messageSelectDTO: {}", messageSelectDTO);
            messages = messageService.getChatMessages(messageSelectDTO);
        }
        return ResponseGenerator.returnOK("消息查询成功", messages);
    }

    @PostMapping("/setIsRead")
    public BaseResponse<Long> setIsRead(@RequestBody MessageSetIsReadDTO messageSetIsReadDTO) {
        messageService.setIsRead(messageSetIsReadDTO);
        return ResponseGenerator.returnOK("设置isRead成功", null);
    }

    @DeleteMapping("/deleteMessage")
    public BaseResponse<Long> deleteMessage(@RequestBody MessageDeleteDTO messageDeleteDTO) {
        messageService.deleteMessage(messageDeleteDTO);
        return ResponseGenerator.returnOK("删除message成功", null);
    }
}
