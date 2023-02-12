package suzumiya.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import suzumiya.constant.CacheConst;
import suzumiya.model.dto.CacheUpdateDTO;
import suzumiya.service.ICacheService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class CacheServiceImpl implements ICacheService {

    @Autowired
    private Cache<String, Object> cache; // Caffeine

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void updateCache(CacheUpdateDTO cacheUpdateDTO) {
        /* 构建或刷新Caffeine和Redis缓存 */
        String key = cacheUpdateDTO.getKey();
        Object value = cacheUpdateDTO.getValue();
        int cacheType = cacheUpdateDTO.getCacheType();
        Duration redisTTL = cacheUpdateDTO.getRedisTTL();
        cache.put(key, value);

        if (cacheType == CacheConst.VALUE_TYPE_SIMPLE) {
            redisTemplate.opsForValue().set(key, value, redisTTL);
        } else {
            Map<String, Object> valueMap = new HashMap<>();
            BeanUtil.beanToMap(value, valueMap, true, null);
            redisTemplate.opsForHash().putAll(key, valueMap);
            redisTemplate.expire(key, redisTTL);
        }
    }

    @Override
    public void clearCache(String keyPattern) {
        /* 清除Caffeine和Redis缓存 */
        Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match(keyPattern).build());
        while (cursor.hasNext()) {
            String cacheKey = cursor.next();
            cache.invalidate(cacheKey);
            redisTemplate.delete(cacheKey);
        }
    }
}
