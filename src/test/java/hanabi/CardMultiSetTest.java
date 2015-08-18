package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class CardMultiSetTest {
    @Test
    public void testIncrementAll() {
        long set = CardMultiSet.EMPTY;
        for (int color = 0; color < Card.NUM_COLORS; color++) {
            for (int number = 0; number < Card.NUM_NUMBERS; number++) {
                int card = Card.create(color, number);
                for (int i = 0; i < (color + number) % 3; i++) {
                    set = CardMultiSet.increment(set, card);
                }
            }
        }
        for (int color = 0; color < Card.NUM_COLORS; color++) {
            for (int number = 0; number < Card.NUM_NUMBERS; number++) {
                int card = Card.create(color, number);
                Assert.assertEquals((color + number) % 3, CardMultiSet.getCount(set, card));
            }
        }
    }

    @Test
    public void testIncrementCount() {
        long set = CardMultiSet.EMPTY;
        int card = Card.create(2, 3);
        Assert.assertEquals(0, CardMultiSet.getCount(set, card));
        for (int i = 1; i <= 3; i++) {
            set = CardMultiSet.increment(set, card);
            Assert.assertEquals(i, CardMultiSet.getCount(set, card));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncrementTooMany() {
        long set = CardMultiSet.EMPTY;
        int card = Card.create(2, 3);
        for (int i = 1; i <= 4; i++) {
            set = CardMultiSet.increment(set, card);
        }
    }
}
