package suzumiya.service;

import com.baomidou.mybatisplus.extension.service.IService;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.pojo.Page;
import suzumiya.model.pojo.Post;

public interface IPostService extends IService<Post> {

    void insert(Post post);

    Page<Post> search(PostSearchDTO postSearchDTO);
}
