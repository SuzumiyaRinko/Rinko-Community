package suzumiya;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestMain {

    public static void main(String[] args) {
        ArrayList<Integer> nums = new ArrayList<>(List.of(1, 2, 3));
        Collections.reverse(nums);
        System.out.println(nums);
    }
}
