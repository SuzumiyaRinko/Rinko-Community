package suzumiya.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import suzumiya.constant.CommonConst;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.CommentMapper;
import suzumiya.mapper.MessageMapper;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.MessageDeleteDTO;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.dto.MessageSelectDTO;
import suzumiya.model.dto.MessageSetIsReadDTO;
import suzumiya.model.pojo.Message;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.MessageSelectVO;
import suzumiya.service.IMessageService;
import suzumiya.service.IUserService;
import suzumiya.util.RedisUtils;
import suzumiya.util.WordTreeUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "userCache")
    private Cache<String, Object> userCache; // Caffeine

    @Autowired
    private IUserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void saveMessage(MessageInsertDTO messageInsertDTO) {
        Message message = new Message();
        Long toUserId = messageInsertDTO.getToUserId();
        message.setToUserId(toUserId);

        if (messageInsertDTO.getIsSystem()) {
            /* 发送系统消息 */
            message.setFromUserId(0L);
            Long targetId = messageInsertDTO.getTargetId();
            Long eventUserId = messageInsertDTO.getEventUserId();
            int systemMsgType = messageInsertDTO.getSystemMsgType();

            // 判断是否已经发送过这种类型的系统消息
            if (systemMsgType != Message.SYSTEM_TYPE_POST_COMMENT && messageMapper.exists(new LambdaQueryWrapper<Message>()
                    .eq(Message::getTargetId, targetId)
                    .eq(Message::getSystemMsgType, systemMsgType)
                    .eq(Message::getEventUserId, eventUserId)
            )) {
                return;
            }

            message.setSystemMsgType(systemMsgType);
            message.setTargetId(targetId);
            message.setEventUserId(eventUserId);

            // 6种系统消息
            if (systemMsgType == Message.SYSTEM_TYPE_POST_LIKE) {
                String title = postMapper.getTitleByPostId(targetId);
                message.setContent("给点赞了你的帖子 \"" + title + "\"");
            } else if (systemMsgType == Message.SYSTEM_TYPE_POST_COLLECT) {
                String title = postMapper.getTitleByPostId(targetId);
                message.setContent("收藏了你的帖子 \"" + title + "\"");
            } else if (systemMsgType == Message.SYSTEM_TYPE_POST_COMMENT) {
                String title = postMapper.getTitleByPostId(targetId);
                message.setContent("评论了你的帖子 \"" + title + "\"");
            } else if (systemMsgType == Message.SYSTEM_TYPE_POST_FOLLOWING) {
                String title = postMapper.getTitleByPostId(targetId);
                message.setContent("你关注的po主发布了一个帖子 \"" + title + "\"");
            } else if (systemMsgType == Message.SYSTEM_TYPE_COMMENT_LIKE) {
                String content = commentMapper.getContentByCommentId(targetId);
                message.setContent("点赞了你的评论 \"" + content + "\"");
            } else if (systemMsgType == Message.SYSTEM_TYPE_COMMENT_RECOMMENT) {
                String content = commentMapper.getContentByCommentId(targetId);
                message.setContent("评论了你的评论 \"" + content + "\"");
            } else if (systemMsgType == Message.SYSTEM_TYPE_SOMEONE_FOLLOWING) {
                message.setContent("关注了你");
            }
        } else {
            /* 判断内容长度 */
            String content = messageInsertDTO.getContent();
            Boolean isSystem = messageInsertDTO.getIsSystem();
            if (!isSystem && StrUtil.isNotBlank(content) && content.length() > 1000) {
                throw new RuntimeException("内容长度超出限制");
            }

            if (StrUtil.isNotBlank(content)) {
                /* 过滤敏感词 */
                content = WordTreeUtils.replaceAllSensitiveWords(content);
                /* 清除HTML标记 */
                content = HtmlUtil.cleanHtmlTag(messageInsertDTO.getContent());
                /* 换行符转换 */
                content = content.replaceAll(CommonConst.REPLACEMENT_ENTER, "<br>");
                messageInsertDTO.setContent(content);
            }

            /* 发送私聊消息 */
            message.setFromUserId(messageInsertDTO.getFromUserId());
            message.setContent(messageInsertDTO.getContent());
            message.setPictures(messageInsertDTO.getPictures());

            /* 更新双方用户的私信列表 */
            if (toUserId > 0) {
                Long myUserId = messageInsertDTO.getMyId();
                double zsScore = RedisUtils.getZSetScoreBy2EpochSecond();
                redisTemplate.opsForZSet().add(RedisConst.USER_MESSAGE_KEY + myUserId, toUserId, zsScore);
                redisTemplate.opsForZSet().add(RedisConst.USER_MESSAGE_KEY + toUserId, myUserId, zsScore);
            }
        }

        /* 保存message到MySQL */
        messageMapper.insert(message);
    }

    @Override
    public Integer notReadCount(Long myUserId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisConst.USER_UNREAD_KEY + myUserId);
        Integer unreadCount = 0;
        if (ObjectUtil.isNotEmpty(entries)) {
            for (Object value : entries.values()) {
                unreadCount += (Integer) value;
            }
        }

        return unreadCount;
    }

    @Override
    public MessageSelectVO getMessages(MessageSelectDTO messageSelectDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();

        List<Message> messages = new ArrayList<>();
        Long lastId = messageSelectDTO.getLastId();

        MessageSelectVO messageSelectVO = new MessageSelectVO();

        if (messageSelectDTO.getIsSystem()) {
            /* 系统消息 */
            lastId = lastId != null ? lastId : Long.MAX_VALUE;
            messages = messageMapper.getSystemMessagesLtId(myUserId, lastId);
            if (messages.size() != 0) {
                lastId = messages.get(messages.size() - 1).getId();
                for (Message message : messages) {
                    message.setEventUser(userService.getSimpleUserById(message.getEventUserId()));
                }
            }
        } else {
            /* 公共聊天室 */
            // 获取最后一次对话内容
            Message lastMessage4Public = messageMapper.getLastMessage4PublicByUserId(myUserId);
            if (lastMessage4Public != null) {
                if (StrUtil.isBlank(lastMessage4Public.getContent())) {
                    lastMessage4Public.setContent("[图片]");
                }
            } else {
                lastMessage4Public = new Message();
            }

            messageSelectVO.setLastMessage4Public(lastMessage4Public);

            /* 私信列表（全部一起查） */
            Set<Object> objects = redisTemplate.opsForZSet().reverseRange(RedisConst.USER_MESSAGE_KEY + myUserId, 0L, -1L);
            if (ObjectUtil.isNotEmpty(objects)) {
                for (Object object : objects) {
                    Long fromUserId = (long) (Integer) object;
                    // 获取最后一次对话内容
                    Message lastMessage = messageMapper.getLastMessageBy2Id(fromUserId, myUserId);
                    if (StrUtil.isBlank(lastMessage.getContent())) {
                        lastMessage.setContent("[图片]");
                    }
                    // 获取对方SimpleUser数据
                    lastMessage.setEventUser(userService.getSimpleUserById(fromUserId));
                    // 获取未读条数
                    Object o = redisTemplate.opsForHash().get(RedisConst.USER_UNREAD_KEY + myUserId, String.valueOf(fromUserId));
                    if (o != null) {
                        lastMessage.setUnreadCount((Integer) o);
                    } else {
                        lastMessage.setUnreadCount(0);
                    }

                    messages.add(lastMessage);
                }
            }
        }

        /* 返回查询结果 */
        messageSelectVO.setMessages(messages);
        messageSelectVO.setLastId(lastId);
        if (messages.size() == 0) {
            messageSelectVO.setIsFinished(true);
        }
        return messageSelectVO;
    }

    @Override
    public MessageSelectVO getChatMessages(MessageSelectDTO messageSelectDTO) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();

        Long targetId = messageSelectDTO.getTargetId();
        Long lastId = messageSelectDTO.getLastId();
        if (lastId == null) {
            lastId = Long.MAX_VALUE;
        }

        /* 查询对话双方的详细Message */
        List<Message> chatMessages = messageMapper.getChatMessagesLtId(myUserId, targetId, lastId);
        if (chatMessages.size() != 0) {
            // pictures转picturesSplit
            for (Message chatMessage : chatMessages) {
                String pictures = chatMessage.getPictures();
                if (StrUtil.isNotBlank(pictures)) {
                    chatMessage.setPicturesSplit(pictures.split("\\|"));
                }
            }
            // 记录lastId
            lastId = chatMessages.get(chatMessages.size() - 1).getId();
        }

        /* 按照时间由旧到新排序 */
        if (ObjectUtil.isNotEmpty(chatMessages)) {
            chatMessages = new ArrayList<>(chatMessages);
            CollectionUtil.reverse(chatMessages);
        }

        /* 查询用户信息 */
        if (targetId == 0) {
            for (Message chatMessage : chatMessages) {
                chatMessage.setEventUser(userService.getSimpleUserById(chatMessage.getFromUserId()));
            }
        }

        /* 返回查询结果 */
        MessageSelectVO messageSelectVO = new MessageSelectVO();
        messageSelectVO.setMessages(chatMessages);
        messageSelectVO.setLastId(lastId);
        if (chatMessages.size() == 0) {
            messageSelectVO.setIsFinished(true);
        }
        return messageSelectVO;
    }

    @Override
    public void setIsRead(MessageSetIsReadDTO messageSetIsReadDTO) {
        /* 获取当前用户id */
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();

        Integer messageType = messageSetIsReadDTO.getMessageType();
        Long targetId = messageSetIsReadDTO.getTargetId();
        Boolean isAll = messageSetIsReadDTO.getIsAll();

        /* 设置已读 */
        if (messageType == 1) {
            LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Message::getIsRead, true);
            updateWrapper.eq(Message::getToUserId, myUserId);
            if (isAll) {
                updateWrapper.eq(Message::getFromUserId, 0);
            } else {
                updateWrapper.eq(Message::getId, targetId);
            }
            messageMapper.update(null, updateWrapper);
        } else if (messageType == 2) {
            if (isAll) {
                redisTemplate.delete(RedisConst.USER_UNREAD_KEY + myUserId);
            } else if (!isAll) {
                redisTemplate.opsForHash().delete(RedisConst.USER_UNREAD_KEY + myUserId, String.valueOf(targetId));
            }
        }
    }

    @Override
    public void deleteMessage(MessageDeleteDTO messageDeleteDTO) {
        /* 获取当前用户id */
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();

        Integer messageType = messageDeleteDTO.getMessageType();
        Long targetId = messageDeleteDTO.getTargetId();
        Boolean isAll = messageDeleteDTO.getIsAll();

        /* 删除消息 / 删除私聊列表 */
        if (messageType == 1) {
            if (!isAll) {
                messageMapper.deleteById(targetId);
            } else if (isAll) {
                messageMapper.delete(new LambdaQueryWrapper<Message>()
                        .eq(Message::getFromUserId, 0)
                        .eq(Message::getToUserId, myUserId));
            }
        } else if (messageType == 2) {
            if (!isAll) {
                redisTemplate.opsForZSet().remove(RedisConst.USER_MESSAGE_KEY + myUserId, targetId); // 私聊列表
                redisTemplate.opsForHash().delete(RedisConst.USER_UNREAD_KEY + myUserId, String.valueOf(targetId)); // 未读条数
            } else if (isAll) {
                redisTemplate.delete(RedisConst.USER_MESSAGE_KEY + myUserId); // 私聊列表
                redisTemplate.delete(RedisConst.USER_UNREAD_KEY + myUserId); // 未读条数
            }
        }
    }
}
