package suzumiya.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;
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
import suzumiya.model.pojo.Message;
import suzumiya.model.pojo.Post;
import suzumiya.model.pojo.User;
import suzumiya.service.ICommentService;
import suzumiya.service.IUserService;
import suzumiya.util.SuzumiyaUtils;

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
    public Long comment(CommentInsertDTO commentInsertDTO) throws JsonProcessingException {
        /* 判断内容长度 */
        if (commentInsertDTO.getContent().length() > 1000) {
            throw new RuntimeException("内容长度超出限制");
        }

        Comment comment = new Comment();

        /* 过滤敏感词 */
        comment.setContent(SuzumiyaUtils.replaceAllSensitiveWords(commentInsertDTO.getContent()));

        /* 清除HTML标记 */
        comment.setContent(HtmlUtil.cleanHtmlTag(comment.getContent()));

        /* 换行符转换 */
        comment.setContent(comment.getContent().replaceAll(CommonConst.REPLACEMENT_ENTER, "<br>"));

        /* 新增comment到MySQL */
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();
        comment.setUserId(myUserId);

        Integer targetType = commentInsertDTO.getType();
        Long targetId = commentInsertDTO.getTargetId();
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);

        // picturesSplit转pictures
        String[] picturesSpilt = commentInsertDTO.getPicturesSplit();
        String pictures = StrUtil.join("|", picturesSpilt);
        comment.setPictures(pictures);
        comment.setPicturesSplit(picturesSpilt);

        commentMapper.insert(comment);

        if (targetType == CommonConst.COMMENT_TYPE_2POST) {
            /* comment数 +1 */
            redisTemplate.opsForValue().increment(RedisConst.POST_COMMENT_COUNT_KEY + targetId);

            /* 添加到待算分Post的Set集合 */
            redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, targetId);

            /* 发送系统消息（异步） */
            Long postId = commentInsertDTO.getTargetId();
            Long toUserId = postMapper.getUserIdByPostId(postId);
            if (!ObjectUtil.equals(myUserId, toUserId)) {
                MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
                messageInsertDTO.setToUserId(toUserId);
                messageInsertDTO.setIsSystem(true);
                messageInsertDTO.setSystemMsgType(Message.SYSTEM_TYPE_POST_COMMENT);
                messageInsertDTO.setTargetId(postId);
                messageInsertDTO.setEventUserId(myUserId);
                rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.MESSAGE_INSERT_KEY, messageInsertDTO);
            }
        } else {
            /* recomment数 +1 */
            redisTemplate.opsForValue().increment(RedisConst.COMMENT_RECOMMENT_COUNT_KEY + targetId);

            /* 发送系统消息（异步） */
            Long commentId = commentInsertDTO.getTargetId();
            Long toUserId = commentMapper.getUserIdByCommentId(commentId);
            if (!ObjectUtil.equals(myUserId, toUserId)) {
                MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
                messageInsertDTO.setToUserId(toUserId);
                messageInsertDTO.setIsSystem(true);
                messageInsertDTO.setSystemMsgType(Message.SYSTEM_TYPE_COMMENT_RECOMMENT);
                messageInsertDTO.setTargetId(commentId);
                messageInsertDTO.setEventUserId(myUserId);
                rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.MESSAGE_INSERT_KEY, messageInsertDTO);
            }
        }

        return comment.getId();
    }

    @Override
    public void delete(Long commentId) {
        Integer targetType = commentMapper.getTargetTypeByCommentId(commentId);
        Long targetId = commentMapper.getTargetIdByCommentId(commentId);

        /* 在MySQL把comment逻辑删除 */
        commentMapper.deleteById(commentId);

        if (targetType == CommonConst.COMMENT_TYPE_2POST) {
            /* 删除该评论下的所有评论 */
            commentMapper.deleteCommentByTargetTypeAndTargetId(2, commentId);
            /* comment数 -1 */
            redisTemplate.opsForValue().decrement(RedisConst.POST_COMMENT_COUNT_KEY + targetId);
            /* 添加到待算分Post的Set集合 */
            redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, targetId);
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
        // 2.1 comment的targetType
        queryWrapper.eq(Comment::getTargetType, targetType);
        // 2.2 comment的targetId
        queryWrapper.eq(Comment::getTargetId, targetId);
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

        for (Comment comment : comments) {
            // 2.4 获取User信息
            User simpleUser = userService.getSimpleUserById(comment.getUserId());
            comment.setCommentUser(simpleUser);
            // 2.5 pictures转picturesSplit
            String pictures = comment.getPictures();
            if (ObjectUtil.isNotEmpty(pictures)) {
                comment.setPicturesSplit(comment.getPictures().split("\\|"));
            } else {
                comment.setPicturesSplit(new String[0]);
            }
            if (targetType == CommentSelectDTO.TARGET_TYPE_POST) {
                // 2.6 返回targetId的前3条comment
                Long id = comment.getId();
                List<String> first3Comments = commentMapper.getFirst3CommentsByTargetId(id);
                comment.setFirst3Comments(first3Comments);
                // 2.7 获取并为comment设置likeCount, commentCount
                Long commentId = comment.getId();
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                Object tmpLikeCount = valueOperations.get(RedisConst.COMMENT_LIKE_COUNT_KEY + commentId);
                Object tmpCommentCount = valueOperations.get(RedisConst.COMMENT_RECOMMENT_COUNT_KEY + commentId);
                int likeCount = 0;
                int commentCount = 0;
                if (tmpLikeCount != null) likeCount = (int) tmpLikeCount;
                if (tmpCommentCount != null) commentCount = (int) tmpCommentCount;
                comment.setLikeCount(likeCount);
                comment.setCommentCount(commentCount);
            }
        }

        // 3 返回结果
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        System.out.println(pageInfo.getTotal());
        return new PageInfo<>(comments);
    }

    @Override
    public Comment getCommentByCommentId(Long commentId) {
        // 1 获取Comment
        Comment comment = commentMapper.selectById(commentId);
        // 2 获取CommentUser
        User simpleUser = userService.getSimpleUserById(comment.getUserId());
        comment.setCommentUser(simpleUser);
        // 3 pictures转picturesSplit
        String pictures = comment.getPictures();
        if (ObjectUtil.isNotEmpty(pictures)) {
            comment.setPicturesSplit(comment.getPictures().split("\\|"));
        } else {
            comment.setPicturesSplit(new String[0]);
        }
        // 4 获取并为comment设置likeCount, commentCount
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object tmpLikeCount = valueOperations.get(RedisConst.COMMENT_LIKE_COUNT_KEY + commentId);
        Object tmpCommentCount = valueOperations.get(RedisConst.COMMENT_RECOMMENT_COUNT_KEY + commentId);
        int likeCount = 0;
        int commentCount = 0;
        if (tmpLikeCount != null) likeCount = (int) tmpLikeCount;
        if (tmpCommentCount != null) commentCount = (int) tmpCommentCount;
        comment.setLikeCount(likeCount);
        comment.setCommentCount(commentCount);

        return comment;
    }

    @Override
    public void like(Long commentId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisConst.COMMENT_LIKE_LIST_KEY + commentId, myUserId))) {
            /* 减少某个comment的点赞数 */
            redisTemplate.opsForValue().decrement(RedisConst.COMMENT_LIKE_COUNT_KEY + commentId);
            /* 在该comment的like列表中移除user */
            redisTemplate.opsForSet().remove(RedisConst.COMMENT_LIKE_LIST_KEY + commentId, myUserId);
        } else {
            /* 增加某个comment的点赞数 */
            redisTemplate.opsForValue().increment(RedisConst.COMMENT_LIKE_COUNT_KEY + commentId);
            /* 在该comment的like列表中新增user */
            redisTemplate.opsForSet().add(RedisConst.COMMENT_LIKE_LIST_KEY + commentId, myUserId);
            /* 发送系统消息（异步） */
            Long toUserId = commentMapper.getUserIdByCommentId(commentId);
            if (!ObjectUtil.equals(myUserId, toUserId)) {
                MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
                messageInsertDTO.setToUserId(toUserId);
                messageInsertDTO.setEventUserId(myUserId);
                messageInsertDTO.setIsSystem(true);
                messageInsertDTO.setSystemMsgType(Message.SYSTEM_TYPE_COMMENT_LIKE);
                messageInsertDTO.setTargetId(commentId);
                rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.MESSAGE_INSERT_KEY, messageInsertDTO);
            }
        }
    }

    @Override
    public Boolean hasLike(Long postId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();
        return redisTemplate.opsForSet().isMember(RedisConst.COMMENT_LIKE_LIST_KEY + postId, myUserId);
    }
}
