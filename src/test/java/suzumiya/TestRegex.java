package suzumiya;

public class TestRegex {

    public static void main(String[] args) {
//        String regex = "[^0-9a-zA-Z]";
//        System.out.println("123".matches(regex));
//        System.out.println("123aAAa12".matches(regex));
//        System.out.println("123&AA".matches(regex));
//        Pattern p = Pattern.compile("[^0-9a-zA-Z]");

//        String str1 = "114514@qq.com";
//        String str2 = "11451AA4@qq.com";
//        String str3 = "11451VV 4@qq.com";
//        String str4 = "11451VV@4@qq.com";
//        System.out.println(str1.matches(CommonConst.REGEX_EMAIL));
//        System.out.println(str2.matches(CommonConst.REGEX_EMAIL));
//        System.out.println(str3.matches(CommonConst.REGEX_EMAIL));
//        System.out.println(str4.matches(CommonConst.REGEX_EMAIL));

//        final String s1 = "11x1";
//        String s2 = s1.replaceAll("x", "666");
//        System.out.println("s1 = " + s1);
//        System.out.println("s2 = " + s2);


        boolean matches = "pwd12345678".matches("[0-9a-zA-Z]{8,16}");
    }
}
