package hanabi;

import org.junit.Assert;
import org.junit.Test;

public class GameStateTest {
    @Test
    public void testInitialState() {
        GameState state = new GameState(true, 4, RandomUtil.INSTANCE);
        Assert.assertEquals(0, state.getScore());
        Assert.assertEquals(GameState.MAX_HINTS, state.getHints());
        Assert.assertEquals(60 - 4 * 4, state.getDeckSize());
        Assert.assertEquals(0, state.getCurrentPlayer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialStateInvalidNumPlayers() {
        new GameState(true, 6, RandomUtil.INSTANCE);
    }

    /**
     * Giving a hint should decrent the hint counter
     */
    @Test
    public void testHint() {
        GameState state = new GameState(true, 4, RandomUtil.INSTANCE);
        int color = Card.getColor(Hand.getCard(state.getHand(1), 0));
        state.applyMove(Move.hintColor(1, color));
        Assert.assertEquals(GameState.MAX_HINTS - 1, state.getHints());
        Assert.assertEquals(1, state.getCurrentPlayer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHintSelf() {
        GameState state = new GameState(true, 4, RandomUtil.INSTANCE);
        int color = Card.getColor(Hand.getCard(state.getHandUnsafe(0), 0));
        state.applyMove(Move.hintColor(0, color));
    }

    @Test(expected = IllegalStateException.class)
    public void testNoHints() {
        GameState state = new GameState(true, 4, RandomUtil.INSTANCE);
        for (int i = 0; i < GameState.MAX_HINTS + 1; i++) {
            int target = (i + 1) % 4;
            int color = Card.getColor(Hand.getCard(state.getHand(target), 0));
            state.applyMove(Move.hintColor(target, color));
        }
    }

    /**
     * Discarding should draw another card and restore a hint.
     */
    @Test
    public void testDiscard() {
        final int TOP_OF_DECK = Card.create(1, 2);
        int[] deck = {Card.create(1, 0), TOP_OF_DECK};
        int hints = 1;
        int lives = 1;
        int[] hands = {Hand.create(Card.create(0, 0))};
        GameState state = new GameState(deck, hints, lives, hands, 0, -1, 0, Tableau.EMPTY);
        state.applyMove(Move.discard(0));
        Assert.assertEquals(1, state.getDeckSize());
        Assert.assertEquals(hints + 1, state.getHints());
        Assert.assertEquals(1, Hand.getSize(state.getHandUnsafe(0)));
        Assert.assertEquals(TOP_OF_DECK, Hand.getCard(state.getHandUnsafe(0), 0));
        Assert.assertEquals(-1, state.getEndingPlayer());
        Assert.assertFalse(state.isFinished());
        Assert.assertEquals("[A0]", CardMultiSet.toString(state.getDiscard()));
    }

    /**
     * Drawing the last card should initiate game end
     */
    @Test
    public void testDiscardDrawLastCard() {
        final int TOP_OF_DECK = Card.create(1, 2);
        int[] deck = {TOP_OF_DECK};
        int hints = 1;
        int[] hands = {Hand.create(Card.create(0, 0))};
        GameState state = new GameState(deck, hints, 1, hands, 0, -1, 0, Tableau.EMPTY);
        state.applyMove(Move.discard(0));
        Assert.assertEquals(0, state.getDeckSize());
        Assert.assertEquals(hints + 1, state.getHints());
        Assert.assertEquals(1, Hand.getSize(state.getHandUnsafe(0)));
        Assert.assertEquals(TOP_OF_DECK, Hand.getCard(state.getHandUnsafe(0), 0));
        Assert.assertEquals(0, state.getEndingPlayer());
        Assert.assertFalse(state.isFinished());
    }

    /**
     * Discarding when the deck is empty should not draw a card
     */
    @Test
    public void testDiscardEmptyDeck() {
        int[] deck = {};
        int hints = 1;
        int[] hands = {Hand.create(Card.create(0, 0)), Hand.create(Card.create(0, 0))};
        int currentPlayer = 1;
        int endingPlayer = 0;
        GameState state = new GameState(deck, hints, 1, hands, currentPlayer, endingPlayer, 0, Tableau.EMPTY);
        state.applyMove(Move.discard(0));
        Assert.assertEquals(0, state.getDeckSize());
        Assert.assertEquals(hints + 1, state.getHints());
        Assert.assertEquals(1, Hand.getSize(state.getHandUnsafe(0)));
        Assert.assertEquals(0, Hand.getSize(state.getHandUnsafe(1)));
        Assert.assertEquals(0, state.getEndingPlayer());
        Assert.assertEquals(0, state.getCurrentPlayer());
        Assert.assertFalse(state.isFinished());
    }

    /**
     * The game should end if the ending player discards
     */
    @Test
    public void testDiscardEndingPlayer() {
        int[] deck = {};
        int hints = 1;
        int[] hands = {Hand.create(Card.create(0, 0)), Hand.create(Card.create(0, 0))};
        int currentPlayer = 0;
        int endingPlayer = 0;
        GameState state = new GameState(deck, hints, 1, hands, currentPlayer, endingPlayer, 0, Tableau.EMPTY);
        state.applyMove(Move.discard(0));
        Assert.assertEquals(0, state.getDeckSize());
        Assert.assertEquals(hints + 1, state.getHints());
        Assert.assertEquals(0, Hand.getSize(state.getHandUnsafe(0)));
        Assert.assertEquals(1, Hand.getSize(state.getHandUnsafe(1)));
        Assert.assertEquals(0, state.getEndingPlayer());
        Assert.assertTrue(state.isFinished());
    }

    /**
     * The game should end if the ending player hints
     */
    @Test
    public void testHintEndingPlayer() {
        int[] deck = {};
        int hints = 1;
        int[] hands = {Hand.create(Card.create(0, 0)), Hand.create(Card.create(0, 0))};
        int currentPlayer = 0;
        int endingPlayer = 0;
        GameState state = new GameState(deck, hints, 1, hands, currentPlayer, endingPlayer, 0, Tableau.EMPTY);
        state.applyMove(Move.hintColor(1, 0));
        Assert.assertEquals(0, state.getDeckSize());
        Assert.assertEquals(hints - 1, state.getHints());
        Assert.assertEquals(1, Hand.getSize(state.getHandUnsafe(0)));
        Assert.assertEquals(1, Hand.getSize(state.getHandUnsafe(1)));
        Assert.assertEquals(0, state.getEndingPlayer());
        Assert.assertTrue(state.isFinished());
    }

    private GameState runSuccessPlayTest(int handCard, int tableau) {
        final int TOP_OF_DECK = Card.create(1, 2);
        final int OLD_HAND_CARD = Card.create(0, 1);
        int[] deck = {Card.create(1, 0), TOP_OF_DECK};
        int hints = 1;
        int lives = 1;
        int[] hands = {Hand.create(OLD_HAND_CARD, handCard)};
        GameState state = new GameState(deck, hints, lives, hands, 0, -1, 0, tableau);
        state.applyMove(Move.play(0));
        Assert.assertEquals(1, state.getDeckSize());
        Assert.assertEquals(2, Hand.getSize(state.getHandUnsafe(0)));
        Assert.assertEquals(TOP_OF_DECK, Hand.getCard(state.getHandUnsafe(0), 0));
        Assert.assertEquals(OLD_HAND_CARD, Hand.getCard(state.getHandUnsafe(0), 1));
        Assert.assertEquals(-1, state.getEndingPlayer());
        Assert.assertFalse(state.isFinished());
        Assert.assertEquals(CardMultiSet.EMPTY, state.getDiscard());
        return state;
    }

    /**
     * Playing successfully should increment the tableau draw a card.
     */
    @Test
    public void testPlaySuccess() {
        GameState state = runSuccessPlayTest(Card.create(0, 0), Tableau.EMPTY);
        Assert.assertEquals(1, state.getHints());
        Assert.assertEquals(1, state.getScore());
        Assert.assertEquals(Tableau.increment(Tableau.EMPTY, 0), state.getTableau());
    }

    /**
     * Filling a stack should increment the tableau and give back a hint.
     */
    @Test
    public void testPlaySuccessFilledStack() {
        GameState state = runSuccessPlayTest(Card.create(0, 4), 4);
        Assert.assertEquals(2, state.getHints());
        Assert.assertEquals(5, state.getScore());
        Assert.assertEquals(5, state.getTableau());
    }

    private GameState runFailedPlayTest(int lives) {
        final int TOP_OF_DECK = Card.create(1, 2);
        final int OLD_HAND_CARD = Card.create(0, 1);
        int[] deck = {Card.create(1, 0), TOP_OF_DECK};
        int hints = 1;
        int[] hands = {Hand.create(OLD_HAND_CARD, Card.create(0, 1))};
        GameState state = new GameState(deck, hints, lives, hands, 0, -1, 0, Tableau.EMPTY);
        state.applyMove(Move.play(0));
        Assert.assertEquals(1, state.getDeckSize());
        Assert.assertEquals(hints, state.getHints());
        Assert.assertEquals(lives - 1, state.getLives());
        Assert.assertEquals(2, Hand.getSize(state.getHandUnsafe(0)));
        Assert.assertEquals(TOP_OF_DECK, Hand.getCard(state.getHandUnsafe(0), 0));
        Assert.assertEquals(OLD_HAND_CARD, Hand.getCard(state.getHandUnsafe(0), 1));
        Assert.assertEquals(-1, state.getEndingPlayer());
        Assert.assertEquals(0, state.getScore());
        Assert.assertEquals(Tableau.EMPTY, state.getTableau());
        Assert.assertEquals("[A1]", CardMultiSet.toString(state.getDiscard()));
        return state;
    }

    /**
     * Playing unsuccessfully should decrement lives and draw a card.
     */
    @Test
    public void testPlayFail() {
        GameState state = runFailedPlayTest(2);
        Assert.assertFalse(state.isFinished());
    }

    /**
     * Playing unsuccessfully on the last life should end the game
     */
    @Test
    public void testPlayFailLastLife() {
        GameState state = runFailedPlayTest(1);
        Assert.assertTrue(state.isFinished());
    }
}
