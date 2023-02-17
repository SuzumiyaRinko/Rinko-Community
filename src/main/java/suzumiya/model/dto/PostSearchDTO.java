package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostSearchDTO implements Serializable {

    private String searchKey; // 正常查询的时候，会用searchKey来查
    private Boolean isSuggestion; // 联想搜索
    private long userId = 0; // 点击别人主页的时候，会用userId来查
    private int[] tagIDs; // 每个搜索结果都要带有这些tagID
    private int sortType = 1; // 1:按综合排序（热度+时间） 2:按热度排 3:按时间排
    private int pageNum = 1;

    public static final int SORT_TYPE_DEFAULT = 1;
    public static final int SORT_TYPE_SCORE = 2;
    public static final int SORT_TYPE_TIME = 3;
}