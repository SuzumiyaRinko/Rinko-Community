package suzumiya.service;

import suzumiya.model.dto.CacheClearDTO;
import suzumiya.model.dto.CacheUpdateDTO;

public interface ICacheService {

    void updateCache(CacheUpdateDTO cacheUpdateDTO);

    void clearCache(CacheClearDTO cacheClearDTO);
}
