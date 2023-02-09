package suzumiya.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import suzumiya.constant.MQConstant;
import suzumiya.mapper.PostMapper;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.pojo.Page;
import suzumiya.model.pojo.Post;
import suzumiya.model.pojo.User;
import suzumiya.service.IPostService;

import javax.annotation.Resource;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void insert(Post post) {
        /* 判断标题和内容长度 */
        if (post.getTitle().length() > 40 || post.getContent().length() > 10000) {
            throw new RuntimeException("标题或内容长度超出限制");
        }

        /* 过滤敏感词（异步） */
        /* 新增post到MySQL（异步） */
        /* 新增post到ES（异步） */
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setUserId(user.getId());
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.POST_INSERT_KEY, post);
    }

    @Override
    public Page<Post> search(PostSearchDTO postSearchDTO) {
        if (StrUtil.isBlank(postSearchDTO.getSearchKey())) {
            // 查询某个用户的post
        } else {
            // 根据searchKey查询相关post
        }

        return null;
    }
}
