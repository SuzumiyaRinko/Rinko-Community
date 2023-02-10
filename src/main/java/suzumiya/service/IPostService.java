package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import suzumiya.model.dto.PostInsertDTO;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.dto.PostUpdateDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.PostSearchVO;

import java.util.List;

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
    List<String> suggest(String searchKey) throws NoSuchFieldException, IllegalAccessException;
}
