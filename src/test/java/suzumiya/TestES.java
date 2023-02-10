package suzumiya;

import cn.hutool.extra.tokenizer.Result;
import cn.hutool.extra.tokenizer.engine.ikanalyzer.IKAnalyzerEngine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestES {

    @Test
    void test() {
        IKAnalyzerEngine engine = new IKAnalyzerEngine();
        Result result = engine.parse("我好了哦");
        List<String> resultList = new ArrayList<>();
        while (result.hasNext()) {
            resultList.add(result.next().getText());
        }
        System.out.println(resultList);
    }
}
