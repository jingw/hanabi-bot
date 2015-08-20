package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class CardCounterTest {
    @Test
    public void testInitialState() {
        final int PLAYERS = 4;
        GameState state = new GameState(false, PLAYERS, RandomUtil.INSTANCE);
        CardCounter counter = new CardCounter(new GameStateView(state, 0));
        for (int i = 0; i < PLAYERS; i++) {
            for (int card = 0; card < 4; card++) {
                Assert.assertEquals(CardCounter.NO_KNOWLEDGE_NO_RAINBOW, counter.getPossibleSet(i, card));
            }
        }
    }

    @Test
    public void testFullyDiscarded() {
        int CARD_TO_DISCARD = Card.create(0, 1);
        int[] deck = {Card.create(0, 2), CARD_TO_DISCARD};
        int hints = 1;
        int lives = 1;
        int[] hands = {Hand.create(Card.create(0, 2), CARD_TO_DISCARD)};
        GameState state = new GameState(false, deck, hints, lives, hands, 0, -1, 0, Tableau.EMPTY);
        CardCounter counter = new CardCounter(new GameStateView(state, 0));

        // should not know about either card
        for (int card = 0; card < 2; card++) {
            Assert.assertEquals(CardCounter.NO_KNOWLEDGE_NO_RAINBOW, counter.getPossibleSet(0, card));
        }

        state.applyMove(Move.discard(0));
        counter.notifyDiscard(CARD_TO_DISCARD, 0, 0);
        counter.notifyDraw(deck[1], 0);
        // should not know about either card
        for (int card = 0; card < 2; card++) {
            Assert.assertEquals(CardCounter.NO_KNOWLEDGE_NO_RAINBOW, counter.getPossibleSet(0, card));
        }

        state.applyMove(Move.discard(0));
        counter.notifyDiscard(CARD_TO_DISCARD, 0, 0);
        counter.notifyDraw(deck[0], 0);

        // should know it's not the discarded card
        for (int card = 0; card < 2; card++) {
            Assert.assertEquals(
                    CardCounter.NO_KNOWLEDGE_NO_RAINBOW & ~CardCounter.cardMask(0, 1),
                    counter.getPossibleSet(0, card));
        }

        Assert.assertEquals(CardCounter.cardMask(0, 1), counter.getKilledCards());
    }

    @Test
    public void testDiscardAndPlay() {
        int CARD_TO_PLAY = Card.create(0, 0);
        int[] deck = {Card.create(0, 2), CARD_TO_PLAY, CARD_TO_PLAY};
        int hints = 1;
        int lives = 1;
        int[] hands = {Hand.create(Card.create(0, 2), CARD_TO_PLAY)};
        GameState state = new GameState(false, deck, hints, lives, hands, 0, -1, 0, Tableau.EMPTY);
        CardCounter counter = new CardCounter(new GameStateView(state, 0));

        // should not know about either card
        for (int card = 0; card < 2; card++) {
            Assert.assertEquals(CardCounter.NO_KNOWLEDGE_NO_RAINBOW, counter.getPossibleSet(0, card));
        }

        state.applyMove(Move.discard(0));
        counter.notifyDiscard(CARD_TO_PLAY, 0, 0);
        counter.notifyDraw(deck[2], 0);
        state.applyMove(Move.discard(0));
        counter.notifyDiscard(CARD_TO_PLAY, 0, 0);
        counter.notifyDraw(deck[1], 0);

        // should not know about either card
        for (int card = 0; card < 2; card++) {
            Assert.assertEquals(CardCounter.NO_KNOWLEDGE_NO_RAINBOW, counter.getPossibleSet(0, card));
        }

        state.applyMove(Move.play(0));
        counter.notifyPlay(CARD_TO_PLAY, 0, 0);
        counter.notifyDraw(deck[0], 0);

        // should know it's the card we just played
        for (int card = 0; card < 2; card++) {
            Assert.assertEquals(
                    CardCounter.NO_KNOWLEDGE_NO_RAINBOW & ~CardCounter.cardMask(0, 0),
                    counter.getPossibleSet(0, card));
        }

        Assert.assertEquals(CardCounter.cardMask(0, 0), counter.getKilledCards());
    }

    @Test
    public void testHints() {
        // TODO this doesn't work with the consistency checks in debug=true
        final int PLAYERS = 4;
        GameState state = new GameState(false, PLAYERS, RandomUtil.INSTANCE);
        CardCounter counter = new CardCounter(new GameStateView(state, 0));
        for (int i = 0; i < PLAYERS; i++) {
            for (int card = 0; card < 4; card++) {
                Assert.assertEquals(CardCounter.NO_KNOWLEDGE_NO_RAINBOW, counter.getPossibleSet(i, card));
            }
        }
        counter.notifyHintColor(0, 1, 0, 0b1010);
        Assert.assertEquals("[A0A1A2A3A4]", counter.getPossibleSetString(0, 1));
        Assert.assertEquals("[A0A1A2A3A4]", counter.getPossibleSetString(0, 3));
        final String NOT_A = "[B0B1B2B3B4C0C1C2C3C4D0D1D2D3D4E0E1E2E3E4]";
        Assert.assertEquals(NOT_A, counter.getPossibleSetString(0, 0));
        Assert.assertEquals(NOT_A, counter.getPossibleSetString(0, 2));

        counter.notifyHintNumber(0, 1, 0, 0b11);
        Assert.assertEquals("[A0]", counter.getPossibleSetString(0, 1));
        Assert.assertEquals("[A1A2A3A4]", counter.getPossibleSetString(0, 3));
        Assert.assertEquals("[B0C0D0E0]", counter.getPossibleSetString(0, 0));
        Assert.assertEquals("[B1B2B3B4C1C2C3C4D1D2D3D4E1E2E3E4]", counter.getPossibleSetString(0, 2));
    }
}
