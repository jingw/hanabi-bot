package hanabi;

/**
 * Plays Hanabi like humans do.
 */
public class HumanStylePlayer extends AbstractPlayer {
    private static final int MAX_TURNS_LEFT_FOR_GAMBLE_PLAY = 4;
    /**
     * Gambling requires disabling some useful asserts, so allow turning it off
     */
    private static final boolean ENABLE_GAMBLING = false;
    private static final boolean ENABLE_FINESSE = true;

    /**
     * Return how many cards a hint represents.
     * Idea: Early in the game, multi-card hints are likely to be useful. Later in the game, they're
     * likely to be noise.
     */
    private int maxCardsPerHint(int type) {
        // 0 = color, 1 = number
        int threshold = type == 0 ? 15 : 1;
        if (state.getScore() < threshold) {
            return 2;
        } else {
            return 1;
        }
    }

    private int[] playQueues;
    private CardCounter counter;

    @Override
    public void notifyGameStarted(GameStateView state, int position) {
        super.notifyGameStarted(state, position);
        playQueues = new int[state.getNumPlayers()];
        counter = new CardCounter(state);
    }

    @Override
    public int getMove() {
        // if there's a queued card to play, play it
        if (playQueues[me] != 0) {
            int position = Integer.numberOfTrailingZeros(playQueues[this.me]);
            return Move.play(position);
        }

        if (state.getHints() > 0) {
            if (ENABLE_FINESSE) {
                int finesse = lookForFinesseHint();
                if (finesse != Move.NULL) {
                    return finesse;
                }
            }

            int hint = lookForHint();
            if (hint != Move.NULL) {
                return hint;
            }
        }

        // if the game is going to end anyways, and I have no useful hint to give, play a card and
        // hope for the best.
        if (state.getTurnsLeft() <= MAX_TURNS_LEFT_FOR_GAMBLE_PLAY && ENABLE_GAMBLING) {
            return Move.play(0);
        }

        // discard the card with the highest chance of being obsolete
        double bestChance = 0;
        int bestIndex = -1;
        int size = state.getMyHandSize();
        for (int i = 0; i < size; i++) {
            int obsolete = counter.countObsolete(me, i);
            int total = counter.numPossiblities(me, i);
            double ratio = (double)obsolete / total;
            if (ratio >= bestChance) {
                bestChance = ratio;
                bestIndex = i;
            }
        }

        return Move.discard(bestIndex);
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
        int maxSearch = Math.min(state.getTurnsLeft(), state.getNumPlayers());
        for (int delta = 1; delta < maxSearch; delta++) {
            int p = (me + delta) % state.getNumPlayers();
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
                            if (count >= maxCardsPerHint(type)) {
                                break;
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
                if (Tableau.isPlayable(futureTableau, card)) {
                    futureTableau = Tableau.increment(futureTableau, Card.getColor(card));
                } else {
                    if (!ENABLE_GAMBLING) {
                        throw new AssertionError();
                    }
                }
            }
        }

        return Move.NULL;
    }

    private int lookForFinesseHint() {
        long cardsAlreadyHinted = getAllHintedCards();
        int tableau = state.getTableau();
        int maxSearch = Math.min(state.getTurnsLeft(), state.getNumPlayers());
        return lookForFinesseHint(1, maxSearch, cardsAlreadyHinted, tableau, CardMultiSet.EMPTY);
    }

    /**
     * @param delta         offset from current player (me)
     * @param maxSearch     how far to look ahead
     * @param hinted        set of everything already hinted
     * @param tableau       current tableau
     * @param firstCardsSet set of all first cards seen so far. used to avoid ambiguous finesse
     * @return a move or Move.NULL
     */
    private int lookForFinesseHint(final int delta, final int maxSearch, final long hinted,
                                   final int tableau, final long firstCardsSet) {
        // TODO fix ambiguous finesse, e.g. players have duplicate first playable cards
        if (delta == maxSearch) {
            return Move.NULL;
        }
        int p = (me + delta) % state.getNumPlayers();
        int hand = state.getHand(p);
        if (playQueues[p] != 0) {
            // to avoid ambiguity between an ordinary chained hint and a finesse, don't allow
            // intermediate play queues
            return Move.NULL;
        }
        // check if the first card is playable, not already hinted
        int firstCard = Hand.getCard(hand, 0);
        long newFirstCardSet = CardMultiSet.increment(firstCardsSet, firstCard);
        if (Tableau.isPlayable(tableau, firstCard)
                && CardMultiSet.getCount(hinted, firstCard) == 0
                && CardMultiSet.getCount(firstCardsSet, firstCard) == 0) {
            log("p%d has playable %s in first position", p, Card.toString(firstCard));
            // look for something to close the finesse
            int tableau2 = Tableau.increment(tableau, Card.getColor(firstCard));
            long hinted2 = CardMultiSet.increment(hinted, firstCard);
            int hint = lookForFinesseFinalizer(
                    delta + 1, maxSearch, hinted2, tableau2, tableau, p, newFirstCardSet);
            if (hint != Move.NULL) {
                return hint;
            }
        }
        // Could not finesse with this card, move on to next player
        return lookForFinesseHint(delta + 1, maxSearch, hinted, tableau, newFirstCardSet);
    }

    private int lookForFinesseFinalizer(final int delta, final int maxSearch, final long hinted,
                                        final int tableau, final int prevTableau,
                                        final int intermediary, final long firstCardsSet) {
        if (delta == maxSearch) {
            return Move.NULL;
        }
        int p = (me + delta) % state.getNumPlayers();
        int hand = state.getHand(p), size = Hand.getSize(hand);
        if (playQueues[p] != 0) {
            // to avoid ambiguity between an ordinary chained hint and a finesse, don't allow
            // intermediate play queues
            return Move.NULL;
        }
        // Go through all possible hints and see if any work with the finesse.
        for (int type = 0; type < 2; type++) {  // 0 = color, 1 = number
            int max = type == 0 ? Card.NUM_COLORS : Card.NUM_NUMBERS;
            loop:
            for (int what = 0; what < max; what++) {
                int count = 0;
                int reliesOnFinesse = 0;
                int futureTableau = tableau;
                for (int i = 0; i < size; i++) {
                    int card = Hand.getCard(hand, i);
                    int number = Card.getNumber(card);
                    int content = type == 0 ? Card.getColor(card) : number;
                    if (content == what) {
                        count++;
                        if (!Tableau.isPlayable(futureTableau, card)) {
                            // not playable
                            continue loop;
                        }
                        if (CardMultiSet.getCount(hinted, card) > 0) {
                            // already hinted
                            continue loop;
                        }
                        if (!Tableau.isPlayable(prevTableau, card)) {
                            reliesOnFinesse++;
                        }
                        futureTableau = Tableau.increment(futureTableau, Card.getColor(card));
                    }
                    if (count >= maxCardsPerHint(type)) {
                        break;
                    }
                }
                if (reliesOnFinesse > 0) {
                    log("Hinting finesse p%d -> p%d", intermediary, p);
                    return type == 0 ? Move.hintColor(p, what) : Move.hintNumber(p, what);
                }
            }
        }
        int firstCard = Hand.getCard(hand, 0);
        if (CardMultiSet.getCount(firstCardsSet, firstCard) > 0) {
            // avoid ambiguity
            return Move.NULL;
        }
        // cannot end finesse with this player. try next one
        return lookForFinesseFinalizer(
                delta + 1, maxSearch, hinted, tableau, prevTableau, intermediary, firstCardsSet);
    }

    @Override
    public void notifyHintColor(int targetPlayer, int sourcePlayer, int color, int which) {
        counter.notifyHintColor(targetPlayer, sourcePlayer, color, which);
        if (playQueues[sourcePlayer] != 0)
            throw new AssertionError();
        // assume a hint is a command to play everything
        playQueues[targetPlayer] = BitVectorUtil.lowestSetBits(which, maxCardsPerHint(0));
        if (which == 0) {
            throw new AssertionError();
        }
        if (ENABLE_FINESSE) {
            checkForFinesse(targetPlayer);
        }
    }

    @Override
    public void notifyHintNumber(int targetPlayer, int sourcePlayer, int number, int which) {
        counter.notifyHintNumber(targetPlayer, sourcePlayer, number, which);
        if (playQueues[sourcePlayer] != 0)
            throw new AssertionError();
        // assume a hint is a command to play everything
        // TODO if card is obviously not playable, interpret as a don't discard hint
        playQueues[targetPlayer] = BitVectorUtil.lowestSetBits(which, maxCardsPerHint(1));
        if (which == 0) {
            throw new AssertionError();
        }
        if (ENABLE_FINESSE) {
            checkForFinesse(targetPlayer);
        }
    }

    private void checkForFinesse(int target) {
        int finesse = interpretFinesse(target);
        if (finesse != -1) {
            log("Detected finesse p%d -> p%d", finesse, target);
            if (playQueues[finesse] != 0)
                throw new AssertionError();
            playQueues[finesse] = 1; // first card
        }
    }

    /**
     * Return the target player if there is a finesse. Otherwise, return 0.
     */
    private int interpretFinesse(int target) {
        // NB: Everyone except the final player will know that finessed player's queue, so there
        // won't be any inconsistencies

        // If the card is not playable, and the prerequisite card is not hinted, then there's a
        // finesse. We require the intermediate card to be not-hinted, and we require that all
        // intermediate players are idle.
        if (target == state.getCurrentPlayer()) {
            // Hinter hinted the player immediately after him
            return -1;
        }
        if (target == me) {
            // If this is a finesse, I'll find out later when someone plays without a hint.
            return -1;
        }
        // We don't allow finesse with intermediate play queues
        if (areThereIntermediatePlayQueues(state.getCurrentPlayer(), target)) {
            return -1;
        }
        if (playQueues[target] == 0)
            throw new AssertionError();
        int hand = state.getHand(target);
        int queue = playQueues[target];
        int tableau = state.getTableau();
        while (queue != 0) {
            int position = Integer.numberOfTrailingZeros(queue);
            queue &= ~(1 << position);
            int targetCard = Hand.getCard(hand, position);
            if (Tableau.isPlayable(tableau, targetCard)) {
                tableau = Tableau.increment(tableau, Card.getColor(targetCard));
            } else {
                // figure out who's card would make this work
                tableau = state.getTableau();
                int p = state.getCurrentPlayer();
                boolean iAmInMiddle = false;
                while (p != target) {
                    if (p == this.me) {
                        iAmInMiddle = true;
                    } else {
                        int firstCard = Hand.getCard(state.getHand(p), 0);
                        if (Tableau.isPlayable(tableau, firstCard)) {
                            int tableau2 = Tableau.increment(tableau, Card.getColor(firstCard));
                            if (Tableau.isPlayable(tableau2, targetCard)) {
                                // found the connector
                                return p;
                            }
                        }
                    }
                    p = (p + 1) % state.getNumPlayers();
                }
                if (!iAmInMiddle)
                    throw new AssertionError("" + this.me);
                // I am the intermediary
                return this.me;
            }
        }
        // every card checked out
        return -1;
    }

    /**
     * Return true if anyone in [start, end) has as play queue.
     */
    private boolean areThereIntermediatePlayQueues(int start, int end) {
        int p = start;
        while (p != end) {
            if (playQueues[p] != 0) {
                return true;
            }
            p = (p + 1) % state.getNumPlayers();
        }
        return false;
    }

    @Override
    public void notifyPlay(int card, int position, int sourcePlayer) {
        counter.notifyPlay(card, position, sourcePlayer);
        if (!ENABLE_GAMBLING) {
            // this algorithm should never blow up
            if (state.getLives() != GameState.MAX_LIVES) {
                throw new AssertionError();
            }
            if ((playQueues[sourcePlayer] & (1 << position)) == 0) {
                if (position != 0 || !ENABLE_FINESSE) {
                    throw new AssertionError();
                }
                log("Assuming p%d was part of a finesse ending on me", sourcePlayer);
            }
        }
        // remove from queue
        playQueues[sourcePlayer] = BitVectorUtil.deleteAndShift(playQueues[sourcePlayer], position);
    }

    @Override
    public void notifyDiscard(int card, int position, int sourcePlayer) {
        counter.notifyDiscard(card, position, sourcePlayer);
        if (playQueues[sourcePlayer] != 0)
            throw new AssertionError();
    }

    @Override
    public void notifyDraw(int card, int sourcePlayer) {
        counter.notifyDraw(card, sourcePlayer);
        playQueues[sourcePlayer] <<= 1;
    }
}
