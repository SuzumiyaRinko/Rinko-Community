package suzumiya;

import cn.hutool.extra.tokenizer.Result;
import cn.hutool.extra.tokenizer.engine.ikanalyzer.IKAnalyzerEngine;

import java.util.ArrayList;
import java.util.List;

public class TestMain {

    public static void main(String[] args) {
//        Integer num = 11;
//        System.out.println(Integer.toBinaryString(num));

//        System.out.println(Integer.parseInt("101", 2));
//        System.out.println(Integer.toBinaryString(5));

//        System.out.println(Integer.toBinaryString(Integer.MAX_VALUE));

//        System.out.println("关于'母猪科学培育'与'母猪的产后护理'的SCI一区论文".length());

//        String t = WordTreeUtils.replaceAllSensitiveWords("x傻逼煞笔打飞机飞机");
//        System.out.println(t);

        IKAnalyzerEngine engine = new IKAnalyzerEngine();
        Result result = engine.parse("来教大家如何实现MySQL安装");
        List<String> resultList = new ArrayList<>();
        while (result.hasNext()) {
            resultList.add(result.next().getText());
        }
        System.out.println(resultList);
    }
}
