package hanabi;

import java.util.Arrays;

/**
 * Card is represented as an int, where the lowest 3 bits are the color, and the next 3 bits are the
 * number. Both of these are 0-based.
 */
public final class Card {
    /**
     * Which color is the special rainbow color
     */
    public static final int RAINBOW_COLOR = 5;
    /**
     * Colors are represented by 0 to 5, inclusive. RAINBOW_COLOR is the special rainbow color.
     */
    public static final int NUM_COLORS = 6;
    /**
     * Numbers are represented by 0 to 4, inclusive
     */
    public static final int NUM_NUMBERS = 5;
    public static final int MAX_NUMBER = 4;
    /**
     * An invalid card
     */
    public static final int NULL = -1;

    /**
     * Array containing all cards
     */
    private static final int[] ALL_CARDS;

    /**
     * How many of each card is in the deck
     */
    public static final int[] NUM_COUNTS = {3, 2, 2, 2, 1};

    static {
        ALL_CARDS = new int[6 * 10];
        int i = 0;
        for (int c = 0; c < NUM_COLORS; c++) {
            for (int n = 0; n < 5; n++) {
                for (int copy = 0; copy < NUM_COUNTS[n]; copy++) {
                    ALL_CARDS[i++] = create(c, n);
                }
            }
        }
    }

    public static int create(int color, int number) {
        validateColor(color);
        validateNumber(number);
        return (number << 3) | color;
    }

    public static int[] makeDeck(boolean rainbow) {
        return Arrays.copyOf(ALL_CARDS, rainbow ? ALL_CARDS.length : ALL_CARDS.length - 10);
    }

    public static int getColor(int card) {
        return card & 0b111;
    }

    public static int getNumber(int card) {
        return card >> 3;
    }

    public static void validate(int card) {
        validateColor(getColor(card));
        validateNumber(getNumber(card));
    }

    public static void validateColor(int color) {
        if (color < 0 || color >= NUM_COLORS) {
            throw new IllegalArgumentException("bad color");
        }
    }

    public static void validateNumber(int number) {
        if (number < 0 || number >= NUM_NUMBERS) {
            throw new IllegalArgumentException("bad number");
        }
    }

    public static String toString(int card) {
        validate(card);
        return "" + colorToChar(getColor(card)) + getNumber(card);
    }

    public static char colorToChar(int color) {
        return (char) (color + 'A');
    }
}
