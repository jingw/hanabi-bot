package hanabi;

/**
 * Uses "Hint all first playables mod 5" strategy.
 */
public class SmartPlayer extends AbstractPlayer {
    private int firstPlayable;
    private int firstDiscardable;
    private int[] otherPlayers;
    private PlayerView[] playerViews;
    private int fives = 0;
    private CardCounter counter;
    private static int[] salt = {1,2,3,4,0};
    private static int[] unsalt = new int[5];
    static {
        for(int i = 0; i < 5; ++i) {
            unsalt[salt[i]] = i;
        }
    }
    private boolean fancy = false;
    @Override
    public void notifyGameStarted(GameStateView stateView, int position) {
        super.notifyGameStarted(stateView, position);
        counter = new CardCounter(state);
        int numPlayers = state.getNumPlayers();
        otherPlayers = new int[numPlayers - 1];
        if (false) {
            int pos = numPlayers - 2;
            for (int i = (this.me + 1) % numPlayers; i != this.me; i =
                    (i + 1) % numPlayers) {
                otherPlayers[pos] = i;
                --pos;
            }
        } else {
            int pos = 0;
            for (int i = (this.me + 1) % numPlayers; i != this.me; i =
                    (i + 1) % numPlayers) {
                otherPlayers[pos] = i;
                ++pos;
            }
        }

        playerViews = new PlayerView[numPlayers];
        for (int i = 0; i < numPlayers; ++i) {
            playerViews[i] = new PlayerView();
        }
        this.firstDiscardable = -1;
        this.firstPlayable = -1;        
    }

    /**
     * This class contains another player's knowledge. It is used to determine
     * what hints will give the most new information.
     * 
     * @author shao
     *
     */
    private class PlayerView {
        int firstPlayable;
        int firstDiscardable;

        public PlayerView() {
            firstPlayable = -1;
            firstDiscardable = -1;
        }
    }

    @Override
    public int getMove() {
        if (isEndgame()) {
            return getMoveEndgame();
        } else {
            return getMoveNormal();
        }
    }
    
    private boolean isEndgame() {
        return state.getDeckSize() < 2;
    }
    
    private int getMoveEndgame() {
        int play = -1;
        if (firstPlayable != -1) {
            play = Move.play(firstPlayable);
        }

        int goodDiscard = -1;
        if (firstDiscardable != -1) {
            goodDiscard = Move.discard(firstDiscardable);
        }
        
        int cardsLeft = state.getDeckSize();
        int hints = state.getHints();
        int lives = state.getLives();
        int playHint = makePlayHint();
        int discardHint = makeDiscardHint();
        int safeDiscard = makeSafeDiscard();

        int numfives = 0;
        int someFive = -1;
        for(int i = 0; i < state.getMyHandSize(); ++i) {
            if ((fives & (1 << i)) > 0) {
                ++numfives;
                someFive = i;
            }
        }
        
        int yolo = -1;
        if (someFive != -1) {
            yolo = Move.play(someFive);
        }

        int playableOnTable = 0;
        for (int player: otherPlayers) {
            if (playerViews[player].firstPlayable != -1) {
                playableOnTable += 1;
            }
        }
        
//        System.out.println("Endgame.");
        if (play != -1 && cardsLeft != 1) {
            return play;
        }
        
        if (play != -1 && cardsLeft == 1) {
            int anyDoubleFives = 0;
            for(int player: otherPlayers) {
                int hand = state.getHand(player);
                int count = 0;
                for (int i = 0; i < Hand.getSize(hand); ++i) {
                    if (Card.getNumber(Hand.getCard(hand, i)) == 4) {
                        ++count;
                    }
                }
                if (count >= 2) {
                    anyDoubleFives += 1;
                }
            }
            if (anyDoubleFives != 2) {
                return play;
            }
        }
        
        int numfours = 0;
        int numtot = 0;
        for(int i = 0; i < 5; ++i) {
            boolean seen = false;
            for(int player: otherPlayers) {
                int hand = state.getHand(player);
                for (int j = 0; j < Hand.getSize(hand); ++j) {
                    int card = Hand.getCard(hand, j);
                    if (Card.getNumber(card) == 4) {
                        if (Card.getColor(card) == i) {
                            seen = true;
                        }
                    }
                }
            }
            int cnt = Tableau.getCount(state.getTableau(), i);
            if (cnt == 5) {
                seen = true;
            }
            
            if (seen) continue;
            ++numtot;
            if (cnt == 4) {
                ++numfours;
            }
        }
        
        double p = (double)(numfours) / numtot;
        if (yolo != -1 && cardsLeft == 0 && (p >= .8 || (lives > 1 && p > 0))) {
            return yolo;
        }

        if (play == -1 && numfives == 1) {

        }
        
        int playHintPower = getPlayHintPower();
        int discardHintPower = getDiscardHintPower();
        if (hints > 0) {
            log("playHint = " + playHint);
            log("playHintPower = " + playHintPower);
            if (playHint != -1 && playHintPower > 0) {
                return playHint;
            }
            if (discardHint != -1) {
                return discardHint;
            }
        }
        
        if (play != -1) {
            return play;
        }

        if (goodDiscard != -1) {
            return goodDiscard;
        }
        return safeDiscard;

    }
    
    private int getMoveNormal() {
        int play = -1;
        if (firstPlayable != -1) {
            play = Move.play(firstPlayable);
        }

        int goodDiscard = -1;
        if (firstDiscardable != -1) {
            goodDiscard = Move.discard(firstDiscardable);
        }

        // if anyone has a playable card, and the deck is thin, hint to buy time
        int hints = state.getHints() - 1; // this does surprisingly well
        int playHint = makePlayHint();
        int discardHint = makeDiscardHint();
        int safeDiscard = makeSafeDiscard();

        // System.out.println("discardHint: " + discardHint);
        // if can prevent life loss, do it.
        boolean willDie = isNextPlayObsolete();

        if (willDie && hints > 0 && playHint != -1 && state.getLives() <= 2) {
//            System.out.println("Saving a life.");
            return playHint;
        }

        // if there's a playable card, play it
        if (play != -1) {
            return play;
        }

        // make a hint
        int playHintPower = getPlayHintPower();
        int discardHintPower = getDiscardHintPower();
        if (hints > 0) {
            if (playHintPower > 0 && playHint != -1) {
                return playHint;
            }
            if (discardHintPower > 2 && discardHint != -1) {
                return discardHint;
            }
        }

        // if there's a discardable card, discard it
        if (goodDiscard != -1 && state.getDeckSize() >= 2) {
            //System.out.println("informed discard");
            return goodDiscard;
        }

        if (hints > 0) {
            int bestHint = discardHint;
            if (bestHint != -1) {
                return bestHint;
            } else {
                //System.out.println("No hint.");
            }
        }

        log("Safe discard.");
        return safeDiscard;
    }

    private int getPlayHintPower() {
        int count = 0;
        int previousCard = -1;
        for (int player : otherPlayers) {
            int playable = getFirstPlayable(player);
            if (playable != -1) {
                int card = Hand.getCard(state.getHand(player), playable);
                if (card == previousCard) {
                    count -= 1;
                    int penalty = 2;
                    int lives = state.getLives();
                    if (penalty > 0 && lives <= 2) {
                        count -= penalty;
                    }
                }
                previousCard = card;
            }
            if (playable != -1 && playerViews[player].firstPlayable == -1) {
                ++count;
            }
        }
        return count;
    }

    private int getDiscardHintPower() {
        int count = 0;
        for (int player : otherPlayers) {
            int discardable = getFirstDiscardable(player);
            if (discardable != -1 && playerViews[player].firstDiscardable == -1
                    && playerViews[player].firstPlayable == -1) {
                ++count;
            }
        }
        return count;
    }

    private boolean isNextPlayObsolete() {
        int nextPlayer = getNextPlayer();
//        int nextNextPlayer = otherPlayers[1];
        int nextPlay = playerViews[nextPlayer].firstPlayable;
        if (nextPlay != -1) {
            //System.out.println("nextPlayer: " + nextPlayer + ", nextPlay: "
            //        + nextPlay);
            int card = Hand.getCard(state.getHand(nextPlayer), nextPlay);
            if (Tableau.isObsolete(state.getTableau(), card)) {
                return true;
            }
        }
        
        return false;
    }

    private int getNextPlayer() {
        return otherPlayers[0];
    }

    private int makeCheatingDiscard() {
        int tableau = state.getTableau();
        int hand = state.getHandUnsafe(me);
//        for (int i = 0; i < Hand.getSize(hand); ++i) {
//            int card = Hand.getCard(hand, i);
//            if (Tableau.isObsolete(tableau, card)) {
//                return Move.discard(i);
//            }
//        }
        for (int i = 0; i < Hand.getSize(hand); ++i) {
            int card = Hand.getCard(hand, i);
            if (Card.getNumber(card) < 4) {
                return Move.discard(i);
            }
        }
        return Move.discard(0);
    }

    private double safeDiscardScore(int i) {
        int bad = counter.countBadDiscards(me, i);
        int total = counter.numPossiblities(me, i);
        return (double) -bad / total;
    }

    private int makeSafeDiscard() {
        // discard the card with the highest chance of being obsolete
        double bestScore = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;
        int size = state.getMyHandSize();
        for (int i = 0; i < size; i++) {
            double score = safeDiscardScore(i);
            // using > gave better performance than >=
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        return Move.discard(bestIndex);
    }
    
    private int makeDiscardHint() {
        int value = sumOfFirstDiscardable(otherPlayers) % 5;
        if (value < 0)
            value += 5;

        for (int player : otherPlayers) {
            int hand = state.getHand(player);
            int size = Hand.getSize(hand);
            for (int i = 0; i < size; ++i) {
                int card = Hand.getCard(hand, i);
                if (Card.getColor(card) == value) {
                    return Move.hintColor(player, value);
                }
            }
        }
        return -1;
    }

    private void inferFirstDiscardable(int hinter, int value) {
        int part = 0;
        for (int player : otherPlayers) {
            if (player == hinter) {
                continue;
            }
            int index = getFirstDiscardable(player);
            playerViews[player].firstDiscardable = index;
            part += index;
        }

        if (hinter == me) {
            return;
        }

        int index = (value - part) % 5;
        if (index < -1) {
            index += 5;
        }
        if (index >= 4) {
            index -= 5;
        }
        firstDiscardable = index;
    }

    private int makePlayHint() {
        int value = (sumOfFirstPlayable(otherPlayers)) % 5;
        if (value < 0)
            value += 5;
        value = salt[value];

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

    private void inferFirstPlayable(int hinter, int value) {
        value = unsalt[value];
        int part = 0;
        for (int player : otherPlayers) {
            if (player == hinter) {
                continue;
            }
            int index = getFirstPlayable(player);
            playerViews[player].firstPlayable = index;
            part += index;
        }
        


        if (hinter == me) {
            return;
        }

        int index = (value - part) % 5;
        if (index < -1) {
            index += 5;
        }
        if (index >= 4) {
            index -= 5;
        }
        firstPlayable = index;
    }

    private int sumOfFirstDiscardable(int[] players) {
        int sum = 0;
        for (int player : players) {
            sum += getFirstDiscardable(player);
        }
        return sum;
    }

    private int sumOfFirstPlayable(int[] players) {
        int sum = 0;
        for (int player : players) {
            sum += getFirstPlayable(player);
        }
        return sum;
    }

    private int discardScore(int card) {
        int tableau = state.getTableau();
        if (Tableau.isObsolete(tableau, card)) {
            return 5;
        }
//        return 4;

        int inDeck = Card.NUM_COUNTS[Card.getNumber(card)];
        int left = inDeck - CardMultiSet.getCount(state.getDiscard(), card);
        return left - 1;
    }
    
    private int getFirstDiscardable(int player) {
        int hand = state.getHand(player);
        int size = Hand.getSize(hand);
        int bestdiscard = -1;
        int bestscore = 0;
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            int score = discardScore(card);
            if (score > bestscore) {
                bestscore = score;
                bestdiscard = i;
            }
        }
        return bestdiscard;
    }

    private int rankPriority(int rank) {
//         return 0;
        if (rank == 4) {
            return 4;
        }
        return rank + 1;
    }

    private int getFirstPlayable(int player) {
        int tableau = state.getTableau();
        int hand = state.getHand(player);
        int size = Hand.getSize(hand);
        int ans = -1;
        int ansPriority = 0;
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            int next = (player + 1) % state.getNumPlayers();
            int prevCard = -1;
            if (next != me && playerViews[next].firstPlayable != -1) {
                prevCard = Hand.getCard(state.getHand(next), playerViews[next].firstPlayable);
            }
            if (card == prevCard && fancy) {
                continue;
            }
            if (Tableau.isPlayable(tableau, card)) {
                int rank = Card.getNumber(card);
                int priority = rankPriority(rank);
                if (ans == -1 || priority < ansPriority) {
                    ans = i;
                    ansPriority = priority;
                }
            }
        }
        return ans;
    }

    @Override
    public void notifyHintColor(int targetPlayer, int sourcePlayer, int color,
            int which) {
        counter.notifyHintColor(targetPlayer, sourcePlayer, color, which);
        // all players other than source now know their first discardable
        inferFirstDiscardable(sourcePlayer, color);
    }

    @Override
    public void notifyHintNumber(int targetPlayer, int sourcePlayer,
            int number, int which) {
        counter.notifyHintNumber(targetPlayer, sourcePlayer, number, which);
        // all players other than source now know their first playable
        inferFirstPlayable(sourcePlayer, number);
        if (targetPlayer == me) {
            if (number == 4) {
                fives = which;
                log("which = " + which);
            }
        }
    }

    @Override
    public void notifyPlay(int card, int position, int player) {
        counter.notifyPlay(card, position, player);
        if (player == this.me) {
            fives = BitVectorUtil.deleteAndShift(fives, position);
            firstPlayable = -1;
        }
        int size = state.getDeckSize();
        if (state.getEndingPlayer() != -1 || state.getEndingPlayer() == player) {
            size += 1;
        }
        if (player != this.me && size > 1 && playerViews[player].firstPlayable != position) {
            if (fancy) {
                int difference = position - playerViews[player].firstPlayable;
                firstPlayable -= difference;
                if (firstPlayable < -1) {
                    firstPlayable += 5;
                }
                if (firstPlayable >= 4) {
                    firstPlayable -= 5;
                }
            }
            
            log("expected " + playerViews[player].firstPlayable);
//            throw new RuntimeException();
            System.out.println("WTF");
//            throw new RuntimeException();
        }
        // System.out.println("resetting playable");
        playerViews[player].firstPlayable = -1;
        if (playerViews[player].firstDiscardable != -1) {
            playerViews[player].firstDiscardable -= 1;
        }
    }

    @Override
    public void notifyDiscard(int card, int position, int player) {
        counter.notifyDiscard(card, position, player);
        if (player == this.me) {
            fives = BitVectorUtil.deleteAndShift(fives, position);
            firstDiscardable = -1;
        }

        playerViews[player].firstDiscardable = -1;
        if (playerViews[player].firstPlayable != -1 && playerViews[player].firstPlayable > position) {
            playerViews[player].firstPlayable -= 1;
        }
    }

    @Override
    public void notifyDraw(int card, int player) {
        counter.notifyDraw(card, player);
        if (player == this.me) {
            fives = fives << 1;
        }

        if (playerViews[player].firstPlayable != -1) {
            playerViews[player].firstPlayable += 1;
        }

        if (playerViews[player].firstDiscardable != -1) {
            playerViews[player].firstDiscardable += 1;
        }
    }

}
