package suzumiya.model.pojo;

import lombok.Data;

@Data
public class History {

    private Integer targetType;
    public static final Integer TYPE_HOME = 1;
    public static final Integer TYPE_POST = 2;
    public static final Integer TYPE_COMMENT = 3;

    private Long targetId = -1L;
    private Integer pageNum = 1;
    private Long scrollTop = 0L;
    private Long lastView = -1L; // 比如在Post界面，会返回lastView的comment
}
