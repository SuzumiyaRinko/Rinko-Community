package suzumiya.model.pojo;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {

    private Integer pageNum; // 当前页
    private Integer pageSize; // 一页有多少条数据
    private Integer total; // 总共有多少数据
    private List<T> data; // 数据
}
