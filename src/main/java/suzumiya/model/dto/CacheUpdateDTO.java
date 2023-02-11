package suzumiya.model.dto;

import lombok.Data;

import java.time.Duration;

@Data
public class CacheUpdateDTO {

    private int cacheType;
    private String key;
    private Object value;
    private Duration redisTTL;
}
