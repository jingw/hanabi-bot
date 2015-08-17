package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class MoveTest {
    @Test
    public void testDiscard() {
        int move = Move.discard(4);
        Move.validate(move);
        Assert.assertEquals(Move.DISCARD, Move.getType(move));
        Assert.assertEquals(4, Move.getPosition(move));
    }

    @Test
    public void testPlay() {
        int move = Move.play(3);
        Move.validate(move);
        Assert.assertEquals(Move.PLAY, Move.getType(move));
        Assert.assertEquals(3, Move.getPosition(move));
    }

    @Test
    public void testHintColor() {
        int move = Move.hintColor(0, 3);
        Move.validate(move);
        Assert.assertEquals(Move.HINT_COLOR, Move.getType(move));
        Assert.assertEquals(0, Move.getHintPlayer(move));
        Assert.assertEquals(3, Move.getHintContent(move));
    }

    @Test
    public void testHintNumber() {
        int move = Move.hintNumber(0, 3);
        Move.validate(move);
        Assert.assertEquals(Move.HINT_NUMBER, Move.getType(move));
        Assert.assertEquals(0, Move.getHintPlayer(move));
        Assert.assertEquals(3, Move.getHintContent(move));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPlayer() {
        Move.hintNumber(5, 3);
    }
}
