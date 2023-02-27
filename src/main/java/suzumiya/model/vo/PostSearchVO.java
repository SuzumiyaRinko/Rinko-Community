package suzumiya.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
public class PostSearchVO implements Serializable {

    private Integer total;
    /*
        如果查post, 返回 List<Post>
        如果联想查询, 返回 Set<String>
     */
    private Collection data;
}
