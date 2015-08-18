package hanabi;

/**
 * Cheats by looking at this player's own hand. Hints are used only to buy time.
 * This represents the best one could possibly do in Hanabi.
 */
public class CheatingPlayer implements Player {
    @Override
    public int getMove(GameState state) {
        int hand = state.getHand(state.getCurrentPlayer());
        int size = Hand.getSize(hand);
        int tableau = state.getTableau();

        // if there's a playable card, play the lowest.
        int minPlayableNum = Integer.MAX_VALUE;
        int minPlayableIndex = -1;
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            int color = Card.getColor(card), number = Card.getNumber(card);
            if (number < minPlayableNum && Tableau.getCount(tableau, color) == number) {
                minPlayableNum = number;
                minPlayableIndex = i;
            }
        }
        if (minPlayableIndex >= 0) {
            return Move.play(minPlayableIndex);
        }

        // if anyone has a playable card, and the deck is thin, hint to buy time
        if (state.getHints() > 0 && state.getDeckSize() <= 5 && doesAnyoneHavePlayableCard(state)) {
            return Move.hintColor(0, 0);
        }

        // if there's an obsolete card, throw it away
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            if (Tableau.isObsolete(tableau, card)) {
                return Move.discard(i);
            }
        }

        // if anyone has a playable card, hint to buy time
        if (state.getHints() > 0 && doesAnyoneHavePlayableCard(state)) {
            return Move.hintColor(0, 0);
        }

        // discard a card already in someone's hand
        long allCards = combineHands(state);
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            if (CardMultiSet.getCount(allCards, card) > 1) {
                return Move.discard(i);
            }
        }

        // discard a card that wouldn't kill the game
        long discard = state.getDiscard();
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            if (CardMultiSet.getCount(discard, card) + 1 < Card.NUM_COUNTS[Card.getNumber(card)]) {
                return Move.discard(i);
            }
        }

        // fall back to discarding oldest. should practically never happen
        return Move.discard(size - 1);
    }

    private boolean doesAnyoneHavePlayableCard(GameState state) {
        int tableau = state.getTableau();
        for (int p = 0; p < state.getNumPlayers(); p++) {
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

    private long combineHands(GameState state) {
        long allCards = CardMultiSet.EMPTY;
        for (int p = 0; p < state.getNumPlayers(); p++) {
            int hand = state.getHand(p);
            int size = Hand.getSize(hand);
            for (int i = 0; i < size; i++) {
                allCards = CardMultiSet.increment(allCards, Hand.getCard(hand, i));
            }
        }
        return allCards;
    }

    @Override
    public void notifyHint(int hint) {
    }

    @Override
    public void notifyPlay(int card, int position, int player) {
    }

    @Override
    public void notifyDiscard(int card, int position, int player) {
    }

    @Override
    public void notifyDraw(int card, int player) {
    }
}
