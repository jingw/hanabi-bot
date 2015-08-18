package hanabi;

/**
 * Played cards
 */
public class Tableau {
    public static final int EMPTY = 0;

    public static int getCount(int tableau, int color) {
        Card.validateColor(color);
        return (tableau >> (color * 3)) & 0b111;
    }

    public static int increment(int tableau, int color) {
        int count = getCount(tableau, color);
        if (count == Card.NUM_NUMBERS) {
            throw new IllegalArgumentException("color is maxed out");
        }
        int inc = 1 << (color * 3);
        return tableau + inc;
    }

    public static String toString(int tableau) {
        StringBuilder result = new StringBuilder();
        result.append('[');
        for (int i = 0; i < Card.NUM_COLORS; i++) {
            if (i != 0) {
                result.append(',');
            }
            int count = getCount(tableau, i);
            if (count == 0) {
                result.append(Card.colorToChar(i));
            } else {
                result.append(Card.toString(Card.create(i, count - 1)));
            }
        }
        result.append(']');
        return result.toString();
    }

    /** Return true if the card is a legal play on the given tableau */
    public static boolean isPlayable(int tableau, int card) {
        int color = Card.getColor(card), number = Card.getNumber(card);
        return getCount(tableau, color) == number;
    }

    /** Return true if the card has already been played on the tableau */
    public static boolean isObsolete(int tableau, int card) {
        int color = Card.getColor(card), number = Card.getNumber(card);
        return getCount(tableau, color) > number;
    }
}
