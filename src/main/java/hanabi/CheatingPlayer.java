package hanabi;

/**
 * Cheats by looking at this player's own hand. Attempts to play any playable card, otherwise discards.
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
        if (state.getHints() > 0 && state.getDeckSize() <= 1) {
            for (int p = 0; p < state.getNumPlayers(); p++) {
                int phand = state.getHand(state.getCurrentPlayer());
                int psize = Hand.getSize(phand);
                for (int i = 0; i < psize; i++) {
                    int card = Hand.getCard(phand, i);
                    if (Tableau.isPlayable(tableau, card)) {
                        return Move.hintColor(0, 0);
                    }
                }
            }
        }

        // if there's an obsolete card, throw it away
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            if (Tableau.isObsolete(tableau, card)) {
                return Move.discard(i);
            }
        }

        // if anyone has a playable card, hint to buy time
        if (state.getHints() > 0) {
            for (int p = 0; p < state.getNumPlayers(); p++) {
                int phand = state.getHand(state.getCurrentPlayer());
                int psize = Hand.getSize(phand);
                for (int i = 0; i < psize; i++) {
                    int card = Hand.getCard(phand, i);
                    if (Tableau.isPlayable(tableau, card)) {
                        return Move.hintColor(0, 0);
                    }
                }
            }
        }

        // fall back to discarding oldest
        return Move.discard(size - 1);
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
