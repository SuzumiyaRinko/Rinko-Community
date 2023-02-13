package suzumiya.model.dto;

import lombok.Data;

import java.util.List;

// 用ES查询就要返回这个Page对象（MySQL查询用PageHelper）
@Data
public class Page<T> {

    private Integer pageNum; // 当前页
    private Integer pageSize; // 一页有多少条数据
    private Integer total; // 总共有多少数据
    private List<T> data; // 数据
}
