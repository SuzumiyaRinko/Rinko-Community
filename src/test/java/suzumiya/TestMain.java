package suzumiya;

import cn.hutool.extra.tokenizer.Result;
import cn.hutool.extra.tokenizer.engine.ikanalyzer.IKAnalyzerEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestMain {

    public static void main(String[] args) throws IOException {
        IKAnalyzerEngine engine = new IKAnalyzerEngine();
        Result result = engine.parse("我好了哦");
        List<String> resultList = new ArrayList<>();
        while (result.hasNext()) {
            resultList.add(result.next().getText());
        }
        System.out.println(resultList);
    }
}
