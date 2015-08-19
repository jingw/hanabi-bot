package hanabi;

/**
 * Uses "Hint all first playables mod 5" strategy.
 */
public class SmartPlayer implements Player {
    private int position;
    private int firstPlayable = -1;
    private int[] otherPlayers;
    private GameStateView state;

    @Override
    public int getMove() {
        // if there's a playable card, play it

        if (firstPlayable != -1) {
            int play = Move.play(firstPlayable);
            firstPlayable = -1;
            return play;
        }

        // if anyone has a playable card, and the deck is thin, hint to buy time
        // value chosen by trial and error
        int hints = state.getHints();
        int playHint = makePlayHint();
        int discard = makeDiscard();
        if (playHint != -1 && hints > 0
                && (state.getDeckSize() <= 11 || hints == GameState.MAX_HINTS)) {
            return playHint;
        }

        if (discard != -1) {
            return discard;
        }

        return discard;

        // // if there's an obsolete / duplicate card, throw it away
        // long handCards = CardMultiSet.EMPTY;
        // for (int i = 0; i < myHandSize; i++) {
        // int card = Hand.getCard(myHand, i);
        // if (Tableau.isObsolete(tableau, card)) {
        // return Move.discard(i);
        // }
        // if (CardMultiSet.getCount(handCards, card) > 0) {
        // return Move.discard(i);
        // }
        // handCards = CardMultiSet.increment(handCards, card);
        // }
        //
        // // if anyone has a playable card, hint to buy time
        // if (state.getHints() > 0 && doesAnyoneHavePlayableCard(state)) {
        // return makeValidHint(state);
        // }
        //
        // // discard a card already in someone's hand
        // long allCards = combineOtherHands(state);
        // for (int i = 0; i < myHandSize; i++) {
        // int card = Hand.getCard(myHand, i);
        // if (CardMultiSet.getCount(allCards, card) > 0) {
        // return Move.discard(i);
        // }
        // }
        //
        // // discard a card that wouldn't kill the game
        // long discard = state.getDiscard();
        // for (int i = 0; i < myHandSize; i++) {
        // int card = Hand.getCard(myHand, i);
        // if (CardMultiSet.getCount(discard, card) + 1 <
        // Card.NUM_COUNTS[Card.getNumber(card)]) {
        // return Move.discard(i);
        // }
        // }
        //
        // // fall back to discarding oldest. should practically never happen
        // return Move.discard(myHandSize - 1);
    }

    private int makeDiscard() {
        return Move.discard(0);
    }

    private int makePlayHint() {
        int value = sumOfFirstPlayable(otherPlayers) % 5;
        if (value < 0)
            value += 5;

        for (int player : otherPlayers) {
            int hand = state.getHand(player);
            int size = Hand.getSize(hand);
            for (int i = 0; i < size; ++i) {
                int card = Hand.getCard(hand, i);
                if (Card.getNumber(card) == value) {
                    return Move.hintNumber(player, value);
                }
            }
        }
        return -1;
    }

    private int inferFirstPlayable(int hinter, int value) {
        int part =
                sumOfFirstPlayable(otherPlayers)
                        - getFirstPlayable(hinter);
        int index = (value - part) % 5;
        if (index < -1) {
            index += 5;
        }
        if (index >= 4) {
            index -= 5;
        }
        return index;
    }

    private int sumOfFirstPlayable(int[] players) {
        int sum = 0;
        for (int player : players) {
            sum += getFirstPlayable(player);
        }
        return sum;
    }

    private int getFirstPlayable(int player) {
        int tableau = state.getTableau();
        int hand = state.getHand(player);
        int size = Hand.getSize(hand);
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            if (Tableau.isPlayable(tableau, card)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void notifyGameStarted(GameStateView stateView, int position) {
        this.state = stateView;
        this.position = position;
        int numPlayers = state.getNumPlayers();
        otherPlayers = new int[numPlayers - 1];
        int pos = 0;
        for (int i = 0; i < numPlayers; ++i) {
            if (i != this.position) {
                otherPlayers[pos] = i;
                ++pos;
            }
        }
    }

    @Override
    public void notifyHintColor(int targetPlayer, int sourcePlayer, int color,
            int which) {
    }

    @Override
    public void notifyHintNumber(int targetPlayer, int sourcePlayer,
            int number, int which) {
        if (sourcePlayer == position) {
            return;
        }
        this.firstPlayable = inferFirstPlayable(sourcePlayer, number);
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
