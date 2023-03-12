package suzumiya.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import suzumiya.constant.CacheConst;
import suzumiya.model.dto.CacheClearDTO;
import suzumiya.model.dto.CacheUpdateDTO;
import suzumiya.service.ICacheService;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class CacheServiceImpl implements ICacheService {

    @Resource(name = "userCache")
    private Cache<String, Object> userCache; // Caffeine

    @Resource(name = "postCache")
    private Cache<String, Object> postCache; // Caffeine

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void updateCache(CacheUpdateDTO cacheUpdateDTO) {
        /* 构建或刷新Caffeine和Redis缓存 */
        String key = cacheUpdateDTO.getKey();
        Object value = cacheUpdateDTO.getValue();
        int cacheType = cacheUpdateDTO.getCacheType();
        int caffeineType = cacheUpdateDTO.getCaffeineType();
        Duration duration;

        // Caffeine
        if (caffeineType == CacheConst.CAFFEINE_TYPE_USER) {
            userCache.put(key, value);
            duration = CacheConst.CACHE_REDIS_USER_TTL();
        } else {
            postCache.put(key, value);
            duration = CacheConst.CACHE_REDIS_POST_TTL();
        }

        // Redis
        if (cacheType == CacheConst.VALUE_TYPE_SIMPLE) {
            redisTemplate.opsForValue().set(key, value, duration);
        } else {
            Map<String, Object> valueMap = new HashMap<>();
            BeanUtil.beanToMap(value, valueMap, true, null);
            redisTemplate.opsForHash().putAll(key, valueMap);
            redisTemplate.expire(key, duration);
        }
    }

    @Override
    public void clearCache(CacheClearDTO cacheClearDTO) {
        String keyPattern = cacheClearDTO.getKeyPattern();
        int caffeineType = cacheClearDTO.getCaffeineType();
        /* 清除Caffeine和Redis缓存 */
        Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match(keyPattern).build());
        if (caffeineType == CacheConst.CAFFEINE_TYPE_USER) {
            while (cursor.hasNext()) {
                String cacheKey = cursor.next();
                userCache.invalidate(cacheKey);
                redisTemplate.delete(cacheKey);
            }
        } else {
            while (cursor.hasNext()) {
                String cacheKey = cursor.next();
                postCache.invalidate(cacheKey);
                redisTemplate.delete(cacheKey);
            }
        }
    }
}
