package hanabi;

public class Hand {
    public static final int EMPTY = -1;
    public static final int MAX_HAND_SIZE = 5;

    public static int append(int hand, int card) {
        int size = getSize(hand);
        if (size >= MAX_HAND_SIZE)
            throw new IllegalArgumentException("size is maxed, cannot append more");
        Card.validate(card);
        return (hand << 6) | card;
    }

    public static int create(int... cards) {
        int hand = Hand.EMPTY;
        for (int c: cards) {
            hand = Hand.append(hand, c);
        }
        return hand;
    }

    public static int getCard(int hand, int position) {
        int card = (hand >> (position * 6)) & 0b111111;
        if (card == 0b111111)
            throw new IllegalArgumentException("No card in that position");
        return card;
    }

    /**
     * Return the hand resulting from discarding the given position. 0 is the most recently appended card.
     */
    public static int discard(int hand, int position) {
        getCard(hand, position);
        int start = position * 6;
        int end = start + 6;
        int maskToStart = (1 << start) - 1;
        int maskFromEnd = ~((1 << end) - 1);
        return (hand & maskToStart) | ((hand & maskFromEnd) >> 6);
    }

    public static int getSize(int hand) {
        int usedBits = Integer.SIZE - Integer.numberOfLeadingZeros(~hand);
        return (usedBits + 5) / 6;
    }

    public static void validatePosition(int position) {
        if (position < 0 || position >= MAX_HAND_SIZE) {
            throw new IllegalArgumentException("bad position");
        }
    }

    public static String toString(int hand) {
        StringBuilder result = new StringBuilder();
        result.append('[');
        int size = getSize(hand);
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                result.append(',');
            }
            result.append(Card.toString(getCard(hand, i)));
        }
        result.append(']');
        return result.toString();
    }
}
