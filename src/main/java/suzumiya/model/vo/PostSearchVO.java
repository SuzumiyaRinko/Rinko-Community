package suzumiya.model.vo;

import lombok.Data;
import suzumiya.model.dto.Page;
import suzumiya.model.pojo.Post;

import java.util.List;
import java.util.Map;

@Data
public class PostSearchVO {

    // 查询结果
    private Page<Post> page;
    // 聚合结果
    private Map<String, List<String>> aggregation;
    // 自动补全结果
    private List<String> suggestion;
}
