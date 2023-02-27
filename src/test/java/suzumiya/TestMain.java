package suzumiya;

import suzumiya.util.IKAnalyzerUtils;

import java.util.List;

public class TestMain {

    public static void main(String[] args) {
        List<String> split = IKAnalyzerUtils.parse("产后护理");
        System.out.println(split);
    }
}
