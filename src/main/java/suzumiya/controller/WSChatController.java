package suzumiya.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import suzumiya.constant.CommonConst;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.MessageMapper;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.Message;
import suzumiya.service.IMessageService;
import suzumiya.service.IUserService;
import suzumiya.service.impl.MessageServiceImpl;
import suzumiya.service.impl.UserServiceImpl;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/wsChat/{myUserId}")
@Component
@Scope("protocol")
@Slf4j
public class WSChatController {

    public static IMessageService messageService;
    public static IUserService userService;
    public static MessageMapper messageMapper;
    public static Cache<String, Object> userCache; // Caffeine
    public static ObjectMapper objectMapper;
    public static RedisTemplate<String, Object> redisTemplate;

    static {
        // 获取IOC容器
        ApplicationContext ioc = SpringUtil.getApplicationContext();
        // 获取SpringBean
        messageService = ioc.getBean(MessageServiceImpl.class);
        userService = ioc.getBean(UserServiceImpl.class);
        messageMapper = ioc.getBean(MessageMapper.class);
        userCache = ioc.getBean("userCache", Cache.class);
        objectMapper = ioc.getBean(ObjectMapper.class);
        redisTemplate = ioc.getBean("redisTemplate", RedisTemplate.class);
    }

    // 记录所有当前在线连接
    private static Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    // 连接创建时被调用
    @OnOpen
    public void onOpen(Session session, @PathParam("myUserId") Long myUserId) {
        // 将当前用户存储在容器中
        sessionMap.put(myUserId, session);

        log.debug("ws连接, userId={}, 当前人数: {}", myUserId, sessionMap.size());
    }

    // 接收到前端发送的数据时被调用（消息中转站）
    @OnMessage
    public void onMessage(Session session, String messageJson, @PathParam("myUserId") Long myUserId) throws JsonProcessingException {
        /* 获取message对象 */
        Message message = objectMapper.readValue(messageJson, Message.class);

        Long toUserId = message.getToUserId();

        /* 对方未读数量+1 */
        if (toUserId > 0) {
            Long newUnreadCount = redisTemplate.opsForHash().increment(RedisConst.USER_UNREAD_KEY + toUserId, String.valueOf(myUserId), 1L);
            message.setUnreadCount(newUnreadCount.intValue());
        }

        /* 获取其他信息 */
        message.setFromUserId(myUserId);
        message.setEventUser(userService.getSimpleUserById(myUserId));

        /* 存储message到MySQL */
        MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
        messageInsertDTO.setMyId(myUserId);
        messageInsertDTO.setFromUserId(myUserId);
        messageInsertDTO.setToUserId(toUserId);
        messageInsertDTO.setContent(message.getContent());
        messageInsertDTO.setIsSystem(false);

        /* picturesSplit转pictures */
        String[] picturesSplit = message.getPicturesSplit();
        if (ObjectUtil.isNotEmpty(picturesSplit)) {
            messageInsertDTO.setPictures(StrUtil.join("|", picturesSplit));
        }

        messageService.saveMessage(messageInsertDTO);


        String content = message.getContent();
        if (StrUtil.isNotBlank(content)) {
            message.setContent(content.replaceAll(CommonConst.REPLACEMENT_ENTER, "<br>"));
        }

        if (toUserId > 0) {
            /* 私信 */
            Session toSession = sessionMap.get(toUserId);
            if (toSession != null) {
                toSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(message));
            }
        } else if (toUserId == 0) {
            /* 公共聊天室 */
            Set<Map.Entry<Long, Session>> entrySet = sessionMap.entrySet();
            for (Map.Entry<Long, Session> entry : entrySet) {
                Long userId = entry.getKey();
                Session tmpSession = entry.getValue();
                // 不用发给自己
                if (!userId.equals(myUserId)) {
                    tmpSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(message));
                }
            }
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
