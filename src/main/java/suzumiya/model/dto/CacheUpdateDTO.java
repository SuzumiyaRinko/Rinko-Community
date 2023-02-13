package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

@Data
public class CacheUpdateDTO implements Serializable {

    private int cacheType;
    private String key;
    private Object value;
    private int caffeineType;
    private Duration redisTTL;
}
