package hanabi;

/**
 * Cheats by looking at this player's own hand. Hints are used only to buy time.
 * This represents the best one could possibly do in Hanabi.
 * (actually could be a bit better with brute force planning)
 */
public class CheatingPlayer extends AbstractPlayer {
    @Override
    public int getMove() {
        int myHand = state.getHandUnsafe(state.getCurrentPlayer());
        int myHandSize = Hand.getSize(myHand);
        int tableau = state.getTableau();

        // if there's a playable card, play it
        for (int i = 0; i < myHandSize; i++) {
            int card = Hand.getCard(myHand, i);
            if (Tableau.isPlayable(tableau, card)) {
                return Move.play(i);
            }
        }

        // if anyone has a playable card, and the deck is thin, hint to buy time
        // value chosen by trial and error
        int hints = state.getHints();
        if (hints > 0 && (state.getDeckSize() <= 11 || hints == GameState.MAX_HINTS)
                && doesAnyoneHavePlayableCard(state)) {
            return makeValidHint(state);
        }

        // if there's an obsolete / duplicate card, throw it away
        long handCards = CardMultiSet.EMPTY;
        for (int i = 0; i < myHandSize; i++) {
            int card = Hand.getCard(myHand, i);
            if (Tableau.isObsolete(tableau, card)) {
                return Move.discard(i);
            }
            if (CardMultiSet.getCount(handCards, card) > 0) {
                return Move.discard(i);
            }
            handCards = CardMultiSet.increment(handCards, card);
        }

        // if anyone has a playable card, hint to buy time
        if (state.getHints() > 0 && doesAnyoneHavePlayableCard(state)) {
            return makeValidHint(state);
        }

        // discard a card already in someone's hand
        long allCards = combineOtherHands(state);
        for (int i = 0; i < myHandSize; i++) {
            int card = Hand.getCard(myHand, i);
            if (CardMultiSet.getCount(allCards, card) > 0) {
                return Move.discard(i);
            }
        }

        // discard a card that wouldn't kill the game
        long discard = state.getDiscard();
        for (int i = 0; i < myHandSize; i++) {
            int card = Hand.getCard(myHand, i);
            if (CardMultiSet.getCount(discard, card) + 1 < Card.NUM_COUNTS[Card.getNumber(card)]) {
                return Move.discard(i);
            }
        }

        // fall back to discarding oldest. should practically never happen
        return Move.discard(myHandSize - 1);
    }

    private boolean doesAnyoneHavePlayableCard(GameStateView state) {
        int tableau = state.getTableau();
        for (int p = 0; p < state.getNumPlayers(); p++) {
            if (p == state.getCurrentPlayer()) {
                continue;
            }
            int hand = state.getHand(p);
            int size = Hand.getSize(hand);
            for (int i = 0; i < size; i++) {
                int card = Hand.getCard(hand, i);
                if (Tableau.isPlayable(tableau, card)) {
                    return true;
                }
            }
        }
        return false;
    }

    private long combineOtherHands(GameStateView state) {
        long allCards = CardMultiSet.EMPTY;
        for (int p = 0; p < state.getNumPlayers(); p++) {
            if (p == state.getCurrentPlayer()) {
                continue;
            }
            int hand = state.getHand(p);
            int size = Hand.getSize(hand);
            for (int i = 0; i < size; i++) {
                allCards = CardMultiSet.increment(allCards, Hand.getCard(hand, i));
            }
        }
        return allCards;
    }

    /**
     * Return some valid hint, used for delaying the game.
     */
    private int makeValidHint(GameStateView state) {
        int nextPlayer = (state.getCurrentPlayer() + 1) % state.getNumPlayers();
        int color = Card.getColor(Hand.getCard(state.getHand(nextPlayer), 0));
        return Move.hintColor(nextPlayer, color);
    }
}
