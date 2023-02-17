package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PostInsertDTO implements Serializable {

    private String title;
    private String content;
    private List<Integer> tagIDs;
}