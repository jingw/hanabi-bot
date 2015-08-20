package hanabi;

/**
 * Plays Hanabi like humans do.
 */
public class HumanStylePlayer extends AbstractPlayer {
    private static final int MAX_TURNS_LEFT_FOR_GAMBLE_PLAY = 4;

    private int[] playQueues;

    @Override
    public void notifyGameStarted(GameStateView state, int position) {
        super.notifyGameStarted(state, position);
        playQueues = new int[state.getNumPlayers()];
    }

    @Override
    public int getMove() {
        // if there's a queued card to play, play it
        if (playQueues[position] != 0) {
            int position = Integer.numberOfTrailingZeros(playQueues[this.position]);
            return Move.play(position);
        }

        if (state.getHints() > 0) {
            int hint = lookForHint();
            if (hint != Move.NULL) {
                return hint;
            }
        }

        // if the game is going to end anyways, and I have no useful hint to give, play a card and
        // hope for the best.
        if (state.getTurnsLeft() <= MAX_TURNS_LEFT_FOR_GAMBLE_PLAY) {
            return Move.play(0);
        }

        // discard second oldest card
        // with 4 player no rainbow:
        // oldest: 19.935 +- 0.014
        // 2nd oldest: 20.099 +- 0.014
        return Move.discard(state.getMyHandSize() - 2);
    }

    /**
     * Return a CardMultiSet containing all hinted cards. These are cards that are already going to be played.
     */
    private long getAllHintedCards() {
        long cardsAlreadyHinted = CardMultiSet.EMPTY;
        for (int p = 0; p < state.getNumPlayers(); p++) {
            if (p == position) {
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
        int maxSearch = Math.min(state.getTurnsLeft(), state.getNumPlayers());
        for (int delta = 1; delta < maxSearch; delta++) {
            int p = (position + delta) % state.getNumPlayers();
            int hand = state.getHand(p), size = Hand.getSize(hand);
            if (playQueues[p] == 0) {
                int bestScore = Integer.MIN_VALUE;
                int bestMove = Move.NULL;

                // go through all possible hints and see if any work
                // playable, not duplicate, not already hinted
                for (int type = 0; type < 2; type++) {  // 0 = color, 1 = number
                    int max = type == 0 ? Card.NUM_COLORS : Card.NUM_NUMBERS;
                    loop:
                    for (int what = 0; what < max; what++) {
                        long futureHinted = cardsAlreadyHinted;
                        int futureTableau2 = futureTableau;
                        int count = 0;
                        int minNumber = Integer.MAX_VALUE;
                        for (int i = 0; i < size; i++) {
                            int card = Hand.getCard(hand, i);
                            int number = Card.getNumber(card);
                            int content = type == 0 ? Card.getColor(card) : number;
                            if (content == what) {
                                count++;
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
                                if (number < minNumber) {
                                    minNumber = number;
                                }
                            }
                        }
                        if (count > 0) {
                            int score = count - 2 * minNumber;
                            log("Hint type %d, content %d, target %d: score=%d, cards=%d, minNumber=%d",
                                    type, what, p, score, count, minNumber);
                            if (score > bestScore) {
                                bestMove = type == 0 ? Move.hintColor(p, what) : Move.hintNumber(p, what);
                                bestScore = score;
                            }
                        }
                    }
                }
                if (bestMove != Move.NULL) {
                    return bestMove;
                }
            } else {
                // simulate what this player will play
                int position = Integer.numberOfTrailingZeros(playQueues[p]);
                int card = Hand.getCard(hand, position);
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
        // remove from queue
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
