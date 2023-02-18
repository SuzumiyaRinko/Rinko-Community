package suzumiya.controller;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import suzumiya.mapper.MessageMapper;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.Message;
import suzumiya.service.IMessageService;
import suzumiya.service.impl.MessageServiceImpl;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/wsChat/{myUserId}")
@Component
@Scope("protocol")
@Slf4j
public class WSChatController {

    public static IMessageService messageService;
    public static MessageMapper messageMapper;
    public static Cache<String, Object> userCache; // Caffeine
    public static ObjectMapper objectMapper;

    static {
        // 获取IOC容器
        ApplicationContext ioc = SpringUtil.getApplicationContext();
        // 获取SpringBean
        messageService = ioc.getBean(MessageServiceImpl.class);
        messageMapper = ioc.getBean(MessageMapper.class);
        userCache = ioc.getBean("userCache", Cache.class);
        objectMapper = ioc.getBean(ObjectMapper.class);
    }

    // 记录所有当前在线连接
    private static Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    // 连接创建时被调用
    @OnOpen
    public void onOpen(Session session, @PathParam("myUserId") Long myUserId) {
        // 将当前用户存储在容器中
        sessionMap.put(myUserId, session);

        log.debug("ws连接, userId={}", myUserId);
    }

    // 接收到前端发送的数据时被调用（消息中转站）
    @OnMessage
    public void onMessage(Session session, String messageJson, @PathParam("myUserId") Long myUserId) throws JsonProcessingException {
        /* 获取message对象 */
        Message message = objectMapper.readValue(messageJson, Message.class);

        /* 存储message到MySQL */
        Long toUserId = message.getToUserId();
        MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
        messageInsertDTO.setMyId(myUserId);
        messageInsertDTO.setFromUserId(myUserId);
        messageInsertDTO.setToUserId(toUserId);
        messageInsertDTO.setContent(message.getContent());
        messageInsertDTO.setIsSystem(false);
        messageService.saveMessage(messageInsertDTO);

        /* 判断是否需要通过ws发送消息给对方 */
        Session toSession;
        if ((toSession = sessionMap.get(toUserId)) != null) {
            toSession.getAsyncRemote().sendText(messageJson);
        }
    }

    // 连接关闭时被调用
    @OnClose
    public void onClose(Session session, @PathParam("myUserId") Long myUserId) {
        // 移除该用户的会话
        sessionMap.remove(myUserId);

        log.debug("ws断开连接, userId={}", myUserId);
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        log.error("ex: {}", ex.getMessage());
        ex.printStackTrace();
    }
}
