package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class HandTest {
    @Test
    public void testEmpty() {
        Assert.assertEquals(0, Hand.getSize(Hand.EMPTY));
    }

    @Test
    public void testAppend() {
        int hand = Hand.append(Hand.EMPTY, Card.create(0, 0));
        Assert.assertEquals(1, Hand.getSize(hand));
        hand = Hand.append(hand, Card.create(1, 2));
        Assert.assertEquals(2, Hand.getSize(hand));
        hand = Hand.append(hand, Card.create(3, 4));
        Assert.assertEquals(3, Hand.getSize(hand));
        hand = Hand.append(hand, Card.create(5, 1));
        Assert.assertEquals(4, Hand.getSize(hand));
        hand = Hand.append(hand, Card.create(0, 0));
        Assert.assertEquals(5, Hand.getSize(hand));

        Assert.assertEquals(Card.create(0, 0), Hand.getCard(hand, 0));
        Assert.assertEquals(Card.create(1, 2), Hand.getCard(hand, 3));
    }

    @Test
    public void testAppendMaxed() {
        int hand = Hand.EMPTY;
        for (int i = 0; i < 5; i++) {
            hand = Hand.append(hand, Card.create(0, 0));
        }
        try {
            Hand.append(hand, Card.create(0, 0));
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCardEmpty() {
        int hand = Hand.EMPTY;
        Hand.getCard(hand, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCardOutOfBounds() {
        int hand = Hand.create(Card.create(0, 0));
        Hand.getCard(hand, 1);
    }

    @Test
    public void testDiscard() {
        int hand = Hand.create(Card.create(0, 0), Card.create(0, 1), Card.create(1, 0));
        Assert.assertEquals(3, Hand.getSize(hand));

        hand = Hand.discard(hand, 1);
        Assert.assertEquals(Card.create(1, 0), Hand.getCard(hand, 0));
        Assert.assertEquals(Card.create(0, 0), Hand.getCard(hand, 1));
        Assert.assertEquals(2, Hand.getSize(hand));

        hand = Hand.discard(hand, 0);
        Assert.assertEquals(Card.create(0, 0), Hand.getCard(hand, 0));
        Assert.assertEquals(1, Hand.getSize(hand));

        hand = Hand.discard(hand, 0);
        Assert.assertEquals(Hand.EMPTY, hand);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testValidateInvalidPosition() {
        Hand.validatePosition(5);
    }
}
