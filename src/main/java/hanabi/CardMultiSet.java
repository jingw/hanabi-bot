package hanabi;

public class CardMultiSet {
    public static final long EMPTY = 0;

    public static int getCount(long set, int card) {
        int color = Card.getColor(card), number = Card.getNumber(card);
        int index = color * Card.NUM_NUMBERS + number;
        return (int) ((set >> (index * 2)) & 0b11);
    }

    public static long increment(long set, int card) {
        int count = getCount(set, card);
        if (count == 3) {
            throw new IllegalArgumentException("card is maxed out");
        }
        int color = Card.getColor(card), number = Card.getNumber(card);
        int index = color * Card.NUM_NUMBERS + number;
        long inc = 1L << (index * 2);
        return set + inc;
    }

    public static String toString(long set) {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (int color = 0; color < Card.NUM_COLORS; color++) {
            for (int number = 0; number < Card.NUM_NUMBERS; number++) {
                int card = Card.create(color, number);
                int count = getCount(set, card);
                if (count > 0) {
                    if (result.length() > 1) {
                        result.append(',');
                    }
                    result.append(Card.toString(card));
                    if (count > 1) {
                        result.append('*');
                        result.append(count);
                    }
                }
            }
        }
        result.append(']');
        return result.toString();
    }
}
