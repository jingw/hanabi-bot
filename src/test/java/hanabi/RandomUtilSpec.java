package hanabi;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class RandomUtilSpec {
    @Test
    public void testShuffle() {
        for (int trial = 0; trial < 100; trial++) {
            int[] stuff = {0, 1, 2, 3, 4, 5};
            RandomUtil.shuffle(stuff, RandomUtil.INSTANCE);
            Arrays.sort(stuff);
            for (int i = 0; i < stuff.length; i++) {
                Assert.assertEquals(i, stuff[i]);
            }
        }
    }
}
