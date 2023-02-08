package suzumiya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import suzumiya.model.dto.StatisticsDTO;
import suzumiya.service.IStatisticsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy:MM:dd");
    private final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Long getRangeUV(StatisticsDTO statisticsDTO) {
        /* 判断null */
        LocalDate start = statisticsDTO.getStart();
        LocalDate end = statisticsDTO.getEnd();
        if(start == null || end == null) {
            return null;
        }

        /* 判断参数是否有效 */
        if(start.isAfter(end)) {
            return null;
        }

        /* 判断 start.isEqual(end) */
        if(start.isEqual(end)) {
            String currKey = "uv:" + start.format(formatter1);
            return redisTemplate.opsForHyperLogLog().size(currKey);
        }

        /* 判断该区间UV是否为过去的时间 */
        String startStr = start.format(formatter2);
        String endStr = end.format(formatter2);
        String unionKey = "uv:" + startStr + "_" + endStr;
        if(end.isBefore(LocalDate.now()) && Boolean.TRUE.equals(redisTemplate.hasKey(unionKey))) {
            return redisTemplate.opsForHyperLogLog().size(unionKey);
        }

        /* 计算区间UV */
        // 获取key集合
        List<String> keys = new ArrayList<>();
        while(start.isBefore(end) || start.isEqual(end)) {
            String currKey = "uv:" + start.format(formatter1);
            keys.add(currKey);
            start = start.plusDays(1L);
        }
        // 计算区间UV
        redisTemplate.opsForHyperLogLog().union(unionKey, keys.toArray(new String[0]));
        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }
}
