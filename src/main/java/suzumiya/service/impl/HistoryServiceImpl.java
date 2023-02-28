package suzumiya.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import suzumiya.constant.RedisConst;
import suzumiya.model.dto.HistorySearchDTO;
import suzumiya.model.pojo.History;
import suzumiya.model.pojo.User;
import suzumiya.service.IHistoryService;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HistoryServiceImpl implements IHistoryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public History getHistory(HistorySearchDTO historySearchDTO) {
        // 获取用户id
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();
        // 获取某个历史
        Integer targetType = historySearchDTO.getTargetType();
        Long targetId = historySearchDTO.getTargetId();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisConst.HISTORY_KEY + myUserId + ":" + targetType + ":" + targetId);
        History history = new History();
        if (ObjectUtil.isNotEmpty(entries)) {
            BeanUtil.fillBeanWithMap(entries, history, null);
        }

        return history;
    }

    @Override
    public void saveHistory(History history) {
        // 获取用户id
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();
        // 存储某个历史
        Integer targetType = history.getTargetType();
        Long targetId = history.getTargetId();
        Map<String, Object> entries = new HashMap<>();
        BeanUtil.beanToMap(history, entries, null);
        redisTemplate.opsForHash().putAll(RedisConst.HISTORY_KEY + myUserId + ":" + targetType + ":" + targetId, entries);
    }
}
