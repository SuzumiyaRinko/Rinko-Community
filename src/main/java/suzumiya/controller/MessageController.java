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

    /*
        /message/setRead/{messageType}/{id}
        例子：/message/setRead/1/1, 表示设置id=1的系统消息为"已读"
             /message/setRead/1/0, 表示设置当前用户的所有系统消息为"已读"
             /message/setRead/2/1, 表示设置所有from_user_id=1的私聊消息为"已读"
             /message/setRead/2/0, 表示设置当前用户的所有私聊消息为"已读"
    */
    @PostMapping("/setIsRead/{messageType}/{id}")
    public BaseResponse<Long> setIsRead(@PathVariable("messageType") Integer messageType, @PathVariable("id") Long id) {
        messageService.setIsRead(messageType, id);
        return ResponseGenerator.returnOK("设置isRead成功", null);
    }

    /*
        /message/deleteMessage/{messageType}/{id}
        例子：/message/deleteMessage/1/1, 表示删除id=1的系统消息
             /message/deleteMessage/1/0, 表示删除当前用户的所有系统消息
             /message/deleteMessage/2/1, 表示删除from_user_id=1的私聊列表（暂时不显示）
             /message/deleteMessage/2/0, 表示删除当前用户的所有私聊列表（暂时不显示）
    */
    @DeleteMapping("/deleteMessage/{messageType}/{id}")
    public BaseResponse<Long> deleteMessage(@PathVariable("messageType") Integer messageType, @PathVariable("id") Long id) {
        messageService.deleteMessage(messageType, id);
        return ResponseGenerator.returnOK("删除message成功", null);
    }
}
