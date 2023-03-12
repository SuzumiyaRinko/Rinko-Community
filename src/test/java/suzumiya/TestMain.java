package suzumiya;

import java.util.ArrayList;
import java.util.List;

public class TestMain {

    public static void main(String[] args) {
//        String s = "AaBbCcDdEdFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789()`~!@#$%^&*-+=_|{}[]:;'<>,.?";
//        int len = s.length();
//        String result = "";
//        Random random = new Random();
//        for (int i = 1; i <= 28; i++) {
//            int idx = random.nextInt(len); // [0, len-1]
//            System.out.print(s.charAt(idx));
//        }
//        System.out.println(UUID.randomUUID());

//        String uuid = "1";
//        uuid = UUID.randomUUID().toString();
//        System.out.println();

        String str = "{\"words\":[\"习近平\",\"泽东\",\"毛泽东\"],\"msg\":\"政治敏感\",\"level\":3},{\"words\":[\"cnm\"],\"msg\":\"低俗辱骂\",\"level\":5}";
        str = str.replaceAll("},\\{", "}{");
        List<String> jsonStrs = new ArrayList<>();
        int index;
        while ((index = str.indexOf("}")) != -1) {
            String t = str.substring(0, index + 1);
            str = str.substring(index + 1);
            jsonStrs.add(t);
        }

        for (String jsonStr : jsonStrs) {
            System.out.println(jsonStr);
        }
    }
}
