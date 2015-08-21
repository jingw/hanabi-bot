package hanabi;

/**
 * Tracks all common knowledge (stuff that everyone knows, everyone knows everyone knows, etc.)
 * Does not attempt to track information that only some people know.
 */
public class CardCounter {
    private static final boolean DEBUG = false;
    private static final boolean DUMP = false;
    public static final int[] COLOR_MASKS;
    public static final int[] NUM_MASKS;
    public static final int NO_KNOWLEDGE = (1 << (Card.NUM_NUMBERS * Card.NUM_COLORS)) - 1;
    public static final int NO_KNOWLEDGE_NO_RAINBOW = (1 << (Card.NUM_NUMBERS * (Card.NUM_COLORS - 1))) - 1;

    static {
        COLOR_MASKS = new int[Card.NUM_COLORS];
        NUM_MASKS = new int[Card.NUM_NUMBERS];
        for (int c = 0; c < Card.NUM_COLORS; c++) {
            int mask = 0;
            for (int n = 0; n < Card.NUM_NUMBERS; n++) {
                mask |= cardMask(c, n);
            }
            COLOR_MASKS[c] = mask;
        }
        for (int n = 0; n < Card.NUM_NUMBERS; n++) {
            int mask = 0;
            for (int c = 0; c < Card.NUM_COLORS; c++) {
                mask |= cardMask(c, n);
            }
            NUM_MASKS[n] = mask;
        }
    }

    /**
     * Array mapping from player number -> card index -> bit vector of possible cards.
     * If an entry is zero, it means the player has no card in that position.
     * <p>
     * Each card is mapped to an index from 0 to 30
     * color * Card.NUM_NUMBERS + number;
     */
    private final int[][] possible;
    /**
     * Bit set of cards that are known to be fully played or discarded.
     */
    private int killedCards;
    private final GameStateView state;
    private final int noKnowledge;

    public CardCounter(GameStateView state) {
        noKnowledge = state.isRainbow() ? NO_KNOWLEDGE : NO_KNOWLEDGE_NO_RAINBOW;

        possible = new int[state.getNumPlayers()][];
        this.state = state;
        for (int i = 0; i < state.getNumPlayers(); i++) {
            possible[i] = new int[state.getHandSize(i)];
            for (int j = 0; j < possible[i].length; j++) {
                possible[i][j] = noKnowledge;
            }
        }
    }

    private void applyHint(int target, int mask, int which) {
        for (int cardIndex = 0; cardIndex < possible[target].length; cardIndex++) {
            if (possible[target][cardIndex] != 0) {
                if (((1 << cardIndex) & which) != 0) {
                    // card matches hint
                    possible[target][cardIndex] &= mask;
                } else {
                    // card does not match hint
                    possible[target][cardIndex] &= ~mask;
                }
                if (possible[target][cardIndex] == 0)
                    throw new AssertionError();
            }
        }
    }

    public void notifyHintColor(int targetPlayer, int sourcePlayer, int color, int which) {
        checkConsistency();
        applyHint(targetPlayer, COLOR_MASKS[color], which);
        checkConsistency();
    }

    public void notifyHintNumber(int targetPlayer, int sourcePlayer, int number, int which) {
        checkConsistency();
        applyHint(targetPlayer, NUM_MASKS[number], which);
        checkConsistency();
    }

    private void applyCardRemoval(int player, int position) {
        // shift over
        for (int i = position; i < possible[player].length - 1; i++) {
            possible[player][i] = possible[player][i + 1];
        }
        possible[player][possible[player].length - 1] = 0;
    }

    /**
     * if card is all in discard or tableau, remove it from everyone's view
     */
    private void removeCardIfMaxed(int card) {
        int count = CardMultiSet.getCount(state.getDiscard(), card);
        if (Tableau.isObsolete(state.getTableau(), card)) {
            count++;
        }
        int n = Card.getNumber(card);
        if (count > Card.NUM_COUNTS[n])
            throw new AssertionError("too many of card");
        if (count == Card.NUM_COUNTS[n]) {
            int c = Card.getColor(card);
            int mask = cardMask(c, n);
            if ((killedCards & mask) != 0)
                throw new AssertionError("card should already be dead");
            killedCards |= mask;
            for (int[] hand : possible) {
                for (int cardIndex = 0; cardIndex < hand.length; cardIndex++) {
                    if (hand[cardIndex] != 0) {
                        hand[cardIndex] &= ~mask;
                        if (hand[cardIndex] == 0)
                            throw new AssertionError();
                    }
                }
            }
        }
    }

    public void notifyPlay(int card, int position, int sourcePlayer) {
        applyCardRemoval(sourcePlayer, position);
        removeCardIfMaxed(card);
    }

    public void notifyDiscard(int card, int position, int sourcePlayer) {
        applyCardRemoval(sourcePlayer, position);
        removeCardIfMaxed(card);
    }

    public void notifyDraw(int card, int sourcePlayer) {
        int[] hand = possible[sourcePlayer];
        if (hand[hand.length - 1] != 0) {
            throw new AssertionError("hand already has max size");
        }
        // shift over
        for (int i = hand.length - 1; i > 0; i--) {
            hand[i] = hand[i - 1];
        }
        hand[0] = noKnowledge & ~killedCards;
        checkConsistency();
    }

    public int getPossibleSet(int player, int cardIndex) {
        return possible[player][cardIndex];
    }

    public String getPossibleSetString(int player, int cardIndex) {
        return setToString(possible[player][cardIndex]);
    }

    public static int cardMask(int color, int number) {
        int index = color * Card.NUM_NUMBERS + number;
        return 1 << index;
    }

    public int getKilledCards() {
        return killedCards;
    }

    public static String setToString(int set) {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (int c = 0; c < Card.NUM_COLORS; c++) {
            for (int n = 0; n < Card.NUM_NUMBERS; n++) {
                if ((set & cardMask(c, n)) != 0) {
                    result.append(Card.toString(Card.create(c, n)));
                }
            }
        }
        result.append(']');
        return result.toString();
    }

    /**
     * Verify that knowledge is consistent with the game state
     */
    private void checkConsistency() {
        if (!DEBUG)
            return;
        dump();
        if (possible.length != state.getNumPlayers())
            throw new AssertionError();
        for (int p = 0; p < possible.length; p++) {
            int handSize = state.getHandSize(p);
            int hand = state.getHandUnsafe(p);
            for (int i = 0; i < handSize; i++) {
                int card = Hand.getCard(hand, i);
                int mask = cardMask(Card.getColor(card), Card.getNumber(card));
                if ((possible[p][i] & mask) == 0) {
                    throw new AssertionError(
                            "P" + p + " card " + Card.toString(card)
                                    + " not in possible set " + getPossibleSetString(p, i));
                }
            }
            for (int i = handSize; i < possible[p].length; i++) {
                if (possible[p][i] != 0) {
                    throw new AssertionError();
                }
            }
        }
    }

    private void dump() {
        if (DUMP) {
            for (int p = 0; p < possible.length; p++) {
                int[] hand = possible[p];
                System.out.printf("P%d: ", p);
                for (int i = 0; i < hand.length; i++) {
                    System.out.print(setToString(hand[i]));
                    System.out.print(" ");
                }
                System.out.println();
            }
        }
    }

    /**
     * Of the possibilities, count how many are obsolete
     */
    public int countObsolete(int player, int index) {
        int set = possible[player][index];
        if (set == 0) {
            throw new IllegalArgumentException("no card there");
        }
        int count = 0;
        for (int c = 0; c < Card.NUM_COLORS; c++) {
            for (int n = 0; n < Card.NUM_NUMBERS; n++) {
                if ((set & cardMask(c, n)) != 0) {
                    if (Tableau.isObsolete(state.getTableau(), Card.create(c, n))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public int numPossiblities(int player, int index) {
        int set = possible[player][index];
        if (set == 0) {
            throw new IllegalArgumentException("no card there");
        }
        return Integer.bitCount(set);
    }
}
