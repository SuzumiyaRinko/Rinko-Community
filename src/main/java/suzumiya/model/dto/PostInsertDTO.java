package suzumiya.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostInsertDTO {

    private String title;
    private String content;
    private List<Integer> tagIDs;
}