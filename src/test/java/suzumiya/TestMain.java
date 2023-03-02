package suzumiya;

import suzumiya.util.IKAnalyzerUtils;

import java.util.List;

public class TestMain {

    public static void main(String[] args) {
        List<String> split = IKAnalyzerUtils.parse("123");
        System.out.println(split);
    }
}
