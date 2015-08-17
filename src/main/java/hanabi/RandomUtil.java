package hanabi;

import java.util.Random;

public class RandomUtil {
    public static final Random INSTANCE = new Random();

    public static void shuffle(int[] arr, Random random) {
        for (int i = 0; i < arr.length - 1; i++) {
            int which = random.nextInt(arr.length - i);
            int temp = arr[i];
            arr[i] = arr[which];
            arr[which] = temp;
        }
    }
}
