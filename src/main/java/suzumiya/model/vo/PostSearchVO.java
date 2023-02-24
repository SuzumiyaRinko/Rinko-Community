package suzumiya.model.vo;

import lombok.Data;
import suzumiya.model.pojo.Post;

import java.io.Serializable;
import java.util.List;

@Data
public class PostSearchVO implements Serializable {

    private Integer total;
    private List<Post> data;
}
