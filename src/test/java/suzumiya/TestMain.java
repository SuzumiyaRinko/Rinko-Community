package suzumiya;

import java.util.Random;

public class TestMain {

    public static void main(String[] args) {
        String s = "AaBbCcDdEdFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789()`~!@#$%^&*-+=_|{}[]:;'<>,.?";
        int len = s.length();
        String result = "";
        Random random = new Random();
        for (int i = 1; i <= 28; i++) {
            int idx = random.nextInt(len); // [0, len-1]
            System.out.print(s.charAt(idx));
        }
    }
}
