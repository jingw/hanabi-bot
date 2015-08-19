package hanabi;

/**
 * Plays Hanabi like humans do.
 */
public class HumanStylePlayer implements Player {
    private GameStateView state;
    private int me;
    private int[] playQueues;

    @Override
    public void notifyGameStarted(GameStateView state, int position) {
        this.state = state;
        me = position;
        playQueues = new int[state.getNumPlayers()];
    }

    @Override
    public int getMove() {
        // if there's a queued card to play, play it
        if (playQueues[me] != 0) {
            int position = Integer.numberOfTrailingZeros(playQueues[me]);
            return Move.play(position);
        }

        if (state.getHints() > 0) {
            int hint = lookForHint();
            if (hint != Move.NULL) {
                return hint;
            }
        }

        // discard oldest
        return Move.discard(state.getMyHandSize() - 1);
    }

    /**
     * Return a CardMultiSet containing all hinted cards. These are cards that are already going to be played.
     */
    private long getAllHintedCards() {
        long cardsAlreadyHinted = CardMultiSet.EMPTY;
        for (int p = 0; p < state.getNumPlayers(); p++) {
            if (p == me) {
                continue;
            }
            int hand = state.getHand(p);
            int size = Hand.getSize(hand);
            int queue = playQueues[p];
            for (int i = 0; i < size; i++) {
                if ((queue & (1 << i)) != 0) {
                    int card = Hand.getCard(hand, i);
                    int count = CardMultiSet.getCount(cardsAlreadyHinted, card);
                    if (count != 0)
                        throw new AssertionError();
                    cardsAlreadyHinted = CardMultiSet.increment(cardsAlreadyHinted, card);
                }
            }
        }
        return cardsAlreadyHinted;
    }

    private int lookForHint() {
        long cardsAlreadyHinted = getAllHintedCards();

        // Look for the nearest player that has a playable card, keeping in mind cards that are about to be played
        int futureTableau = state.getTableau();
        for (int delta = 1; delta < state.getNumPlayers(); delta++) {
            int p = (me + delta) % state.getNumPlayers();
            int hand = state.getHand(p), size = Hand.getSize(hand);
            if (playQueues[p] == 0) {
                // go through all possible hints and see if any work
                // playable, not duplicate, not already hinted
                for (int type = 0; type < 2; type++) {  // 0 = color, 1 = number
                    int max = type == 0 ? Card.NUM_COLORS : Card.NUM_NUMBERS;
                    loop:
                    for (int what = 0; what < max; what++) {
                        long futureHinted = cardsAlreadyHinted;
                        int futureTableau2 = futureTableau;
                        boolean isLegalHint = false;
                        for (int i = 0; i < size; i++) {
                            int card = Hand.getCard(hand, i);
                            int content = type == 0 ? Card.getColor(card) : Card.getNumber(card);
                            if (content == what) {
                                isLegalHint = true;
                                if (!Tableau.isPlayable(futureTableau2, card)) {
                                    // not playable
                                    continue loop;
                                }
                                if (CardMultiSet.getCount(futureHinted, card) > 0) {
                                    // already hinted
                                    continue loop;
                                }
                                futureTableau2 = Tableau.increment(futureTableau2, Card.getColor(card));
                                futureHinted = CardMultiSet.increment(futureHinted, card);
                            }
                        }
                        if (isLegalHint) {
                            return type == 0 ? Move.hintColor(p, what) : Move.hintNumber(p, what);
                        }
                    }
                }
            } else {
                // simulate what this player will play
                int position = Integer.numberOfTrailingZeros(playQueues[p]);
                int card = Hand.getCard(hand, position);
                if (!Tableau.isPlayable(futureTableau, card)) {
                    throw new AssertionError();
                }
                futureTableau = Tableau.increment(futureTableau, Card.getColor(card));
            }
        }

        return Move.NULL;
    }

    @Override
    public void notifyHintColor(int targetPlayer, int sourcePlayer, int color, int which) {
        // assume a hint is a command to play everything
        playQueues[targetPlayer] = which;
        if (which == 0) {
            throw new AssertionError();
        }
    }

    @Override
    public void notifyHintNumber(int targetPlayer, int sourcePlayer, int number, int which) {
        // assume a hint is a command to play everything
        // TODO if card is obviously not playable, interpret as a don't discard hint
        playQueues[targetPlayer] = which;
        if (which == 0) {
            throw new AssertionError();
        }
    }

    @Override
    public void notifyPlay(int card, int position, int sourcePlayer) {
        // this algorithm should never blow up
        if (state.getLives() != GameState.MAX_LIVES) {
            throw new AssertionError();
        }
        // remove from queue
        if ((playQueues[sourcePlayer] & (1 << position)) == 0)
            throw new AssertionError();
        playQueues[sourcePlayer] = BitVectorUtil.deleteAndShift(playQueues[sourcePlayer], position);
    }

    @Override
    public void notifyDiscard(int card, int position, int sourcePlayer) {
        if (playQueues[sourcePlayer] != 0)
            throw new AssertionError();
    }

    @Override
    public void notifyDraw(int card, int sourcePlayer) {
        playQueues[sourcePlayer] <<= 1;
    }
}
