package suzumiya.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.CommentMapper;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.CommentInsertDTO;
import suzumiya.model.dto.CommentSelectDTO;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.Comment;
import suzumiya.model.pojo.Post;
import suzumiya.model.pojo.User;
import suzumiya.service.ICommentService;
import suzumiya.service.IUserService;
import suzumiya.util.WordTreeUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

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

    @Override
    public void comment(CommentInsertDTO commentInsertDTO) {
        /* 判断内容长度 */
        if (commentInsertDTO.getContent().length() > 2000) {
            throw new RuntimeException("内容长度超出限制");
        }

        Comment comment = new Comment();

        /* 过滤敏感词 */
        comment.setContent(WordTreeUtils.replaceAllSensitiveWords(commentInsertDTO.getContent()));

        /* 清除HTML标记 */
//        comment.setContent(HtmlUtil.cleanHtmlTag(comment.getContent()));

        /* 新增comment到MySQL */

        //TODO 这两行代码不应该被注释掉
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        comment.setUserId(user.getId());
        comment.setUserId(1L); // 这行代码应该被注释掉

        Integer targetType = commentInsertDTO.getType();
        Long targetId = commentInsertDTO.getTargetId();
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        commentMapper.insert(comment);

        if (targetType == CommonConst.COMMENT_TYPE_2POST) {
            /* comment数 +1 */
            redisTemplate.opsForValue().increment(RedisConst.POST_COMMENT_COUNT_KEY + targetId);

            /* 添加到待算分Post的Set集合 */
            redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, targetId);

            /* 发送系统消息（异步） */
            Long postId = commentInsertDTO.getTargetId();
            Long toUserId = postMapper.getUserIdByPostId(postId);

            MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
            messageInsertDTO.setToUserId(toUserId);
            messageInsertDTO.setIsSystem(true);
            messageInsertDTO.setSystemMsgType(MessageInsertDTO.SYSTEM_TYPE_COMMENT);
            messageInsertDTO.setPostId(postId);
            rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.MESSAGE_INSERT_KEY, messageInsertDTO);
        }
    }

    @Override
    public void delete(Long commentId) {
        Integer targetId = commentMapper.getTargetIdByCommentId(commentId);
        Integer targetType = commentMapper.getTargetTypeByCommentId(commentId);

        /* 在MySQL把comment逻辑删除 */
        commentMapper.deleteById(commentId);

        if (targetType == CommonConst.COMMENT_TYPE_2POST) {
            /* comment数 -1 */
            redisTemplate.opsForValue().decrement(RedisConst.POST_COMMENT_COUNT_KEY + targetId);
            /* 添加到待算分Post的Set集合 */
            redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, commentId);
        }
    }

    @Override
    public PageInfo<Comment> select(CommentSelectDTO commentSelectDTO) {
        /* targetId判空 */
        if (commentSelectDTO.getTargetId() == null) {
            throw new RuntimeException("targetId为null");
        }

        Long targetId = commentSelectDTO.getTargetId();
        Integer selectType = commentSelectDTO.getSelectType();
        Integer targetType = commentSelectDTO.getTargetType();
        int sortType = commentSelectDTO.getSortType();
        int pageNum = commentSelectDTO.getPageNum();

        /* 查询结果并返回 */
        // 1 判断目标是否存在
        Post existedPost = null;
        Comment existedComment = null;
        if (targetType == CommentSelectDTO.TARGET_TYPE_POST) {
            // 对post的评论
            existedPost = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getId, targetId));
            if (existedPost == null) {
                throw new RuntimeException("要评论的post不存在");
            }
        } else {
            // 对comment的评论
            existedComment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>().eq(Comment::getId, targetId));
            if (existedComment == null) {
                throw new RuntimeException("要评论的comment不存在");
            }
        }

        // 2 查询结果并返回
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        // 2.1 comment的target类型
        queryWrapper.eq(Comment::getTargetType, targetType);
        // 如果是对post的评论
        if (targetType == CommentSelectDTO.TARGET_TYPE_POST) {
            // 判断查询类型是 "所有"还是"只看楼主"
            if (selectType == CommentSelectDTO.SELECT_TYPE_POSTER) {
                queryWrapper.eq(Comment::getUserId, existedPost.getUserId());
            }
            // 判断查询的时间顺序
            if (sortType == CommentSelectDTO.SORT_TYPE_REVERSE) {
                queryWrapper.orderByDesc(Comment::getCreateTime);
            }
        }
        // 2.2 分页
        PageHelper.startPage(pageNum, CommonConst.STANDARD_PAGE_SIZE);
        // 2.3 查询
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        // 2.4 获取User信息
        for (Comment comment : comments) {
            User simpleUser = userService.getSimpleUserById(comment.getUserId());
            comment.setCommentUser(simpleUser);
        }
        // 2.5 判断是否需要返回targetId的前3条comment
        if (targetType == CommentSelectDTO.TARGET_TYPE_POST) {
            for (Comment comment : comments) {
                Long id = comment.getId();
                List<String> first3Comments = commentMapper.getFirst3CommentsByTargetId(id);
                comment.setFirst3Comments(first3Comments);
            }
        }

        // 3 返回结果
        return new PageInfo<>(comments);
    }
}
