package hanabi;

/**
 * Uses "Hint all first playables mod 5" strategy.
 */
public class SmartPlayer implements Player {
    private int position;
    private int firstPlayable;
    private int firstDiscardable;
    private int[] otherPlayers;
    private GameStateView state;
    private PlayerView[] playerViews;

    @Override
    public void notifyGameStarted(GameStateView stateView, int position) {
        this.state = stateView;
        this.position = position;
        int numPlayers = state.getNumPlayers();
        otherPlayers = new int[numPlayers - 1];
        int pos = 0;
        for (int i = (this.position + 1) % numPlayers; i != this.position; i =
                (i + 1) % numPlayers) {
            otherPlayers[pos] = i;
            ++pos;
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
        int play = -1;
        if (firstPlayable != -1) {
            play = Move.play(firstPlayable);
            firstPlayable = -1;
        }

        int goodDiscard = -1;
        if (firstDiscardable != -1) {
            goodDiscard = Move.discard(firstDiscardable);
            firstDiscardable = -1;
        }

        // if anyone has a playable card, and the deck is thin, hint to buy time
//      int hints = state.getHints() - 4; // this does surprisingly well
      int hints = state.getHints();
        int playHint = makePlayHint();
        int discardHint = makeDiscardHint();
        int randomDiscard = makeRandomDiscard();

        // System.out.println("discardHint: " + discardHint);
        // if can prevent life loss, do it.
        boolean willDie = isNextPlayObsolete();

        if (willDie && hints > 0 && playHint != -1 && state.getLives() <= 3) {
            System.out.println("Saving a life.");
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
            System.out.println("informed discard");
            return goodDiscard;
        }

        if (hints > 0) {
            int bestHint = discardHint;
            if (bestHint != -1) {
                return bestHint;
            } else {
                System.out.println("No hint.");
            }
        }

        System.out.println("Random discard.");
        return randomDiscard;
    }

    private int getPlayHintPower() {
        int count = 0;
        int previousCard = -1;
        int distance = 0;
        for (int player : otherPlayers) {
            int playable = getFirstPlayable(player);
            if (playable != -1) {
                int card = Hand.getCard(state.getHand(player), playable);
                if (card == previousCard) {
                    count -= 1;
                    int penalty = 3 - distance * 3;
                    if (penalty > 0) {
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
            System.out.println("nextPlayer: " + nextPlayer + ", nextPlay: "
                    + nextPlay);
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

    private int makeRandomDiscard() {
        return Move.discard(0);
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

        if (hinter == position) {
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
        int value = (position + sumOfFirstPlayable(otherPlayers)) % 5;
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

    private void inferFirstPlayable(int hinter, int value) {
        int part = hinter;
        for (int player : otherPlayers) {
            if (player == hinter) {
                continue;
            }
            int index = getFirstPlayable(player);
            playerViews[player].firstPlayable = index;
            part += index;
        }

        if (hinter == position) {
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

    private int getFirstDiscardable(int player) {
        int tableau = state.getTableau();
        int hand = state.getHand(player);
        int size = Hand.getSize(hand);
        for (int i = 0; i < size; i++) {
            int card = Hand.getCard(hand, i);
            if (Tableau.isObsolete(tableau, card)) {
                return i;
            }
        }
        return -1;
    }

    private int rankPriority(int rank) {
        // return 0;
        if (rank == 4)
            return 0;
        else
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
        // all players other than source now know their first discardable
        inferFirstDiscardable(sourcePlayer, color);
    }

    @Override
    public void notifyHintNumber(int targetPlayer, int sourcePlayer,
            int number, int which) {
        // all players other than source now know their first playable
        inferFirstPlayable(sourcePlayer, number);
    }

    @Override
    public void notifyPlay(int card, int position, int player) {
        // System.out.println("resetting playable");
        playerViews[player].firstPlayable = -1;
        if (playerViews[player].firstDiscardable != -1) {
            playerViews[player].firstDiscardable -= 1;
        }
    }

    @Override
    public void notifyDiscard(int card, int position, int player) {
        playerViews[player].firstDiscardable = -1;
        if (playerViews[player].firstPlayable != -1) {
            playerViews[player].firstPlayable -= 1;
        }
    }

    @Override
    public void notifyDraw(int card, int player) {
        if (playerViews[player].firstPlayable != -1) {
            playerViews[player].firstPlayable += 1;
        }

        if (playerViews[player].firstDiscardable != -1) {
            playerViews[player].firstDiscardable += 1;
        }
    }

}
