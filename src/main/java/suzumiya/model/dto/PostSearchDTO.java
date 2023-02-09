package suzumiya.model.dto;

import lombok.Data;

@Data
public class PostSearchDTO {

    private String searchKey; // 正常查询的时候，会用searchKey来查
    private Long userId; // 点击别人主页的时候，会用userId来查
    private Integer[] tagIDs;
    private Integer sortType; // 1:按热度排 2:按时间排
    private Integer pageNum;
    private Integer pageSize = 10;
}