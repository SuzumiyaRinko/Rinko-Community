package suzumiya.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
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
        Long toUserId = messageInsertDTO.getToUserId();
        message.setToUserId(toUserId);

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
            /* 更新双方用户的私信列表 */
            //TODO 这行代码应该被注释
//        Long myId = 1L;
            //TODO 这行代码不应该被注释
            Long myId = messageInsertDTO.getMyId();

            double zsScore = RedisUtils.getZSetScoreBy2EpochSecond();
            redisTemplate.opsForZSet().add(RedisConst.USER_MESSAGE_KEY + myId, toUserId, zsScore);
            redisTemplate.opsForZSet().add(RedisConst.USER_MESSAGE_KEY + toUserId, myId, zsScore);
        }

        /* 保存message到MySQL */
        messageMapper.insert(message);
    }

    @Override
    public long notReadCount(Boolean isSystem, Long myId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        // 未读
        queryWrapper.eq(Message::getIsRead, false);
        // 发给当前用户的消息
        queryWrapper.eq(Message::getToUserId, myId);
        if (isSystem) {
            queryWrapper.eq(Message::getFromUserId, 0);
        } else {
            queryWrapper.ne(Message::getFromUserId, 0);
        }
        return messageMapper.selectCount(queryWrapper);
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
                    // 获取最后一次对话内容
                    Message firstMessage = messageMapper.getFirstMessageBy2Id(fromUserId, myId);
                    if (firstMessage != null) {
                        // 获取对方SimpleUser数据
                        firstMessage.setFromUser(userMapper.getSimpleUserById(fromUserId));
                    }
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
        lastId = chatMessages.get(chatMessages.size() - 1).getId();
        if (ObjectUtil.isNotEmpty(chatMessages)) {
            // 按照时间由旧到新排序
            chatMessages = new ArrayList<>(chatMessages);
            CollectionUtil.reverse(chatMessages);
        }

        /* 返回查询结果 */
        MessageSelectVO messageSelectVO = new MessageSelectVO();
        messageSelectVO.setMessages(chatMessages);
        messageSelectVO.setLastId(lastId);
        return messageSelectVO;
    }
}
