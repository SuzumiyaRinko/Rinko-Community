package suzumiya.model.dto;

import lombok.Data;

@Data
public class PostSearchDTO {

    private String searchKey; // 正常查询的时候，会用searchKey来查
    private Boolean isSuggestion; // 联想搜索
    private Long userId; // 点击别人主页的时候，会用userId来查
    private Integer[] tagIDs;
    private Integer sortType = 1; // 1:按综合排序（热度+时间） 2:按热度排 3:按时间排
    private Integer pageNum = 1;
}