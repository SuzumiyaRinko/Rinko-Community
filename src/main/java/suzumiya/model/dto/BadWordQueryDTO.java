package suzumiya.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadWordQueryDTO {

    private String[] words;
    private String msg;
    private Integer level;
}
