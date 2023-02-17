package suzumiya.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.CommentMapper;
import suzumiya.mapper.MessageMapper;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.dto.MessageSelectDTO;
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
import java.util.Set;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate; // RabbitMQ

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
    public void sendMessage(MessageInsertDTO messageInsertDTO) {
        /* 判断内容长度 */
        if (!messageInsertDTO.getIsSystem() && messageInsertDTO.getContent().length() > 2000) {
            throw new RuntimeException("内容长度超出限制");
        }

        /* 过滤敏感词 */
        messageInsertDTO.setContent(WordTreeUtils.replaceAllSensitiveWords(messageInsertDTO.getContent()));

        Message message = new Message();

        if (messageInsertDTO.getIsSystem()) {
            message.setFromUserId(0L);
            User eventUser = userService.getSimpleUserById(messageInsertDTO.getEventUserId());
            String title = postMapper.getTitleByPostId(messageInsertDTO.getPostId());
            int systemMsgType = messageInsertDTO.getSystemMsgType();
            // 3种系统消息
            if (systemMsgType == MessageInsertDTO.SYSTEM_TYPE_LIKE) {
                message.setPostId(messageInsertDTO.getPostId());
                message.setContent(eventUser.getNickname() + " 给你的帖子 \"" + title + "\" 点了个赞");
            } else if (systemMsgType == MessageInsertDTO.SYSTEM_TYPE_COLLECT) {
                message.setPostId(messageInsertDTO.getPostId());
                message.setContent(eventUser.getNickname() + " 收藏了你的帖子 \"" + title + "\"");
            } else if (systemMsgType == MessageInsertDTO.SYSTEM_TYPE_COMMENT) {
                message.setPostId(messageInsertDTO.getPostId());
                message.setContent(eventUser.getNickname() + " 评论了你的帖子");
            } else if (systemMsgType == MessageInsertDTO.SYSTEM_TYPE_FOLLOWING_POST) {
                message.setPostId(messageInsertDTO.getPostId());
                message.setContent("你关注的po主 \"" + eventUser.getNickname() + "\" 发布了一个帖子 \"" + title + "\"");
            }
        } else {
            message.setFromUserId(messageInsertDTO.getFromUserId());
            message.setContent(messageInsertDTO.getContent());
        }

        Long toUserId = messageInsertDTO.getToUserId();
        message.setToUserId(toUserId);

        /* 保存message到MySQL */
        messageMapper.insert(message);

        /* 更新双方用户的私信列表 */
        //TODO 这两行代码不应该被注释掉
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = user.getId();
//        Long myId = 1L; // 这行代码应该被注释掉
        double zsScore = RedisUtils.getZSetScoreBy2EpochSecond();
        redisTemplate.opsForZSet().add(RedisConst.USER_MESSAGE_KEY + myId, toUserId, zsScore);
        redisTemplate.opsForZSet().add(RedisConst.USER_MESSAGE_KEY + toUserId, myId, zsScore);
    }

    @Override
    public long notReadCount(Boolean isSystem) {
        if (isSystem) {
            return messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getIsRead, false).eq(Message::getFromUser, 0));
        } else {
            return messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getIsRead, false).ne(Message::getFromUser, 0));
        }
    }

    @Override
    public MessageSelectVO getMessages(MessageSelectDTO messageSelectDTO) {
        //TODO 这两行代码不应该被注释掉
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = user.getId();
//        Long myId = 1L; // 这行代码应该被注释掉
        List<Message> messages = new ArrayList<>();
        Long lastId = null;

        MessageSelectVO messageSelectVO = new MessageSelectVO();

        if (messageSelectDTO.getIsSystem()) {
            // 系统消息
            lastId = messageSelectDTO.getLastId();
            messages = messageMapper.getSystemMessagesLtId(myId, lastId);
            lastId = messages.get(messages.size() - 1).getId();
        } else {
            // 私信列表
            Set<Object> objects = redisTemplate.opsForZSet().reverseRange(RedisConst.USER_MESSAGE_KEY + myId, 0L, -1L);
            if (ObjectUtil.isNotEmpty(objects)) {
                for (Object object : objects) {
                    Long fromUserId = (long) (Integer) object;
                    Message firstMessage = messageMapper.getFirstMessageBy2Id(fromUserId, myId);
                    firstMessage.setFromUser(userMapper.getSimpleUserById(fromUserId));
                    messages.add(firstMessage);
                }
            }
        }

        /* 返回查询结果 */
        messageSelectVO.setMessages(messages);
        messageSelectVO.setLastId(lastId);
        return messageSelectVO;
    }

    @Override
    public MessageSelectVO getChatMessages(MessageSelectDTO messageSelectDTO) {
        //TODO 这两行代码不应该被注释掉
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myId = user.getId();
//        Long myId = 1L; // 这行代码应该被注释掉

        Long targetId = messageSelectDTO.getTargetId();
        Long lastId = messageSelectDTO.getLastId();

        /* 查询对话双方的详细Message */
        List<Message> chatMessages = messageMapper.getChatMessagesLtId(myId, targetId, lastId);

        /* 返回查询结果 */
        MessageSelectVO messageSelectVO = new MessageSelectVO();
        lastId = chatMessages.get(chatMessages.size() - 1).getId();
        messageSelectVO.setMessages(chatMessages);
        messageSelectVO.setLastId(lastId);
        return messageSelectVO;
    }
}
