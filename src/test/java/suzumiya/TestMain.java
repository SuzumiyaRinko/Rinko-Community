package suzumiya;

import suzumiya.constant.CommonConst;

public class TestMain {
    public static void main(String[] args) {
        System.out.println("12AA678".matches(CommonConst.REGEX_PASSWORD));
        System.out.println("12345A A678".matches(CommonConst.REGEX_PASSWORD));
        System.out.println("12345A%A678".matches(CommonConst.REGEX_PASSWORD));
    }
}
