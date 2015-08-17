package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class CardTest {
    @Test
    public void testCreateGet() {
        int card = Card.create(3, 2);
        Assert.assertEquals(3, Card.getColor(card));
        Assert.assertEquals(2, Card.getNumber(card));
        Card.validate(card);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBadColor() {
        int card = Card.create(-1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBadNuumber() {
        int card = Card.create(3, -1);
    }

    @Test
    public void testMakeDeckNonRainbow() {
        int[] cards = Card.makeDeck(false);
        Assert.assertEquals(50, cards.length);
        for (int c: cards) {
            Assert.assertNotEquals(Card.getColor(c), Card.RAINBOW_COLOR);
        }
    }

    @Test
    public void testMakeDeckRainbow() {
        int[] cards = Card.makeDeck(true);
        Assert.assertEquals(60, cards.length);
        int numRainbow = 0;
        for (int c: cards) {
            if (Card.getColor(c) == Card.RAINBOW_COLOR) {
                numRainbow++;
            }
        }
        Assert.assertEquals(10, numRainbow);
    }
}
