package suzumiya.model.vo;

import lombok.Data;
import suzumiya.model.dto.Page;
import suzumiya.model.pojo.Post;

import java.io.Serializable;
import java.util.Set;

@Data
public class PostSearchVO implements Serializable {

    // 查询结果
    private Page<Post> page;
    // 聚合结果
//    private Map<String, List<String>> aggregations;
    // 自动补全结果
    private Set<String> suggestions;
}
