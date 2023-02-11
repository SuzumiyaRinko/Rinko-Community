package suzumiya.util;

import cn.hutool.extra.tokenizer.Result;
import cn.hutool.extra.tokenizer.engine.ikanalyzer.IKAnalyzerEngine;

import java.util.ArrayList;
import java.util.List;

public class IKAnalyzerUtils {

    /* 对content进行分词 */
    public static List<String> parse(String content) {
        IKAnalyzerEngine engine = new IKAnalyzerEngine();
        Result t = engine.parse(content);
        List<String> result = new ArrayList<>();
        while (t.hasNext()) {
            result.add(t.next().getText());
        }
        return result;
    }
}
