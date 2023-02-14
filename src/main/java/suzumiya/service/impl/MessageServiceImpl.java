package suzumiya.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import suzumiya.mapper.CommentMapper;
import suzumiya.mapper.MessageMapper;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.Message;
import suzumiya.model.pojo.User;
import suzumiya.service.IMessageService;
import suzumiya.service.IUserService;
import suzumiya.util.WordTreeUtils;

import javax.annotation.Resource;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate; // RabbitMQ

    @Resource(name = "userCache")
    private Cache<String, Object> userCache; // Caffeine

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private IUserService userService;

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
            User simpleUser = userService.getSimpleUserById(messageInsertDTO.getToUserId());
            String title = postMapper.getTitleByPostId(messageInsertDTO.getPostId());
            int systemMsgType = messageInsertDTO.getSystemMsgType();
            // 3种系统消息
            if (systemMsgType == MessageInsertDTO.SYSTEM_TYPE_LIKE) {
                message.setPostId(messageInsertDTO.getPostId());
                message.setContent(simpleUser.getNickname() + " 给你的帖子 \"" + title + "\" 点了个赞");
            } else if (systemMsgType == MessageInsertDTO.SYSTEM_TYPE_COLLECT) {
                message.setPostId(messageInsertDTO.getPostId());
                message.setContent(simpleUser.getNickname() + " 收藏了你的帖子 \"" + title + "\"");
            } else if (systemMsgType == MessageInsertDTO.SYSTEM_TYPE_COMMENT) {
                message.setPostId(messageInsertDTO.getPostId());
                message.setContent(simpleUser.getNickname() + " 评论了你的帖子");
            }
        } else {
            message.setFromUserId(messageInsertDTO.getFromUserId());
            message.setContent(messageInsertDTO.getContent());
        }

        message.setToUserId(messageInsertDTO.getToUserId());

        messageMapper.insert(message);
    }
}
