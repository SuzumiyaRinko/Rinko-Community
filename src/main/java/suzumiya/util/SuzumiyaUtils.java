package suzumiya.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import suzumiya.model.dto.BadWordQueryDTO;

import java.util.ArrayList;
import java.util.List;

public class SuzumiyaUtils {

    public static ObjectMapper objectMapper;
    public static RestTemplate restTemplate;

    static {
        // 获取IOC容器
        ApplicationContext ioc = SpringUtil.getApplicationContext();
        // 获取SpringBean
        objectMapper = ioc.getBean(ObjectMapper.class);
        restTemplate = ioc.getBean(RestTemplate.class);
    }

    /* 顺为数据 */
    public static final String BAD_WORD_FILTER_URL = "https://api.itapi.cn/api/badword/query";
    public static final String BAD_WORD_FILTER_KEY = "WXeeisnLe9rrnazninQhfPkPAM";
    public static final Integer BAD_LEVEL_TERROR = 1;
    public static final Integer BAD_LEVEL_POLITICS = 3;

    public static String replaceAllSensitiveWords(String str) throws JsonProcessingException {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("key", SuzumiyaUtils.BAD_WORD_FILTER_KEY);
        requestBody.add("word", str);
        String result = restTemplate.postForObject(BAD_WORD_FILTER_URL, requestBody, String.class);

        JsonNode jsonNode = objectMapper.readTree(result);
        String sensitiveWordsArr = jsonNode.findValue("data").findValue("data").toString();
        sensitiveWordsArr = sensitiveWordsArr.substring(1, sensitiveWordsArr.length() - 1);
        sensitiveWordsArr = sensitiveWordsArr.replaceAll("},\\{", "}{");

        List<String> jsonStrs = new ArrayList<>();
        int index;
        while ((index = sensitiveWordsArr.indexOf("}")) != -1) {
            String t = sensitiveWordsArr.substring(0, index + 1);
            sensitiveWordsArr = sensitiveWordsArr.substring(index + 1);
            jsonStrs.add(t);
        }

        List<BadWordQueryDTO> badWordQueryDTOs = new ArrayList<>();
        for (String jsonStr : jsonStrs) {
            BadWordQueryDTO badWordQueryDTO = objectMapper.readValue(jsonStr, BadWordQueryDTO.class);
            Integer level = badWordQueryDTO.getLevel();
            if (BAD_LEVEL_TERROR.equals(level) || BAD_LEVEL_POLITICS.equals(level)) {
                badWordQueryDTOs.add(badWordQueryDTO);
            }
        }

        for (BadWordQueryDTO badWordQueryDTO : badWordQueryDTOs) {
            String[] words = badWordQueryDTO.getWords();
            for (String word : words) {
                String replacement = StrUtil.repeat('*', word.length());
                str = str.replaceAll(word, replacement);
            }
        }

        return str;
    }
}
