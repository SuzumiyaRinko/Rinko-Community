package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.PostSearchVO;

public interface IPostService extends IService<Post> {

    void insert(Post post);

    PostSearchVO search(PostSearchDTO postSearchDTO) throws NoSuchFieldException, IllegalAccessException;
}
