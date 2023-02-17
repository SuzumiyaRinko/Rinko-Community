package suzumiya.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

@Data
public class CacheUpdateDTO implements Serializable {

    private Integer cacheType;
    private String key;
    private Object value;
    private Integer caffeineType;
    private Duration duration;
}
