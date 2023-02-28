package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HistorySearchDTO implements Serializable {

    private Integer targetType;
    private Long targetId;
}