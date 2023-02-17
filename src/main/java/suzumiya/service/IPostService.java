package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import suzumiya.model.dto.PostInsertDTO;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.dto.PostUpdateDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.PostSearchVO;

import java.util.List;
import java.util.Set;

public interface IPostService extends IService<Post> {

    /* 新增 */
    void insert(PostInsertDTO postInsertDTO);

    /* 删除 */
    void delete(Long postId);

    /* 更新 */
    void update(PostUpdateDTO postUpdateDTO);

    /* 查询 */
    PostSearchVO search(PostSearchDTO postSearchDTO) throws NoSuchFieldException, IllegalAccessException;

    /* 搜索联想 */
    Set<String> suggest(String searchKey) throws NoSuchFieldException, IllegalAccessException;

    /* 加精 */
    void setWonderful(List<Long> postIds);

    /* 点赞或取消点赞 */
    void like(Long postId);

    /* 收藏或取消收藏 */
    void collect(Long postId);

    /* 根据postId获取post */
    Post getPostByPostId(Long postId);
}
