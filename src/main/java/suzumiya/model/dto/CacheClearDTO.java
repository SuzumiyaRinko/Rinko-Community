package suzumiya.model.dto;

import lombok.Data;

@Data
public class CacheClearDTO {

    private String keyPattern;
    private int caffeineType;
}
