package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CacheClearDTO implements Serializable {

    private String keyPattern;
    private int caffeineType;
}
