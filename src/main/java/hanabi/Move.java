package hanabi;

/**
 * Encode moves into ints
 */
public final class Move {
    public static final int DISCARD = 0;
    public static final int PLAY = 1;
    public static final int HINT_COLOR = 2;
    public static final int HINT_NUMBER = 3;

    public static int discard(int position) {
        Hand.validatePosition(position);
        return (position << 2) | DISCARD;
    }

    public static int play(int position) {
        Hand.validatePosition(position);
        return (position << 2) | PLAY;
    }

    public static int hintColor(int player, int color) {
        Card.validateColor(color);
        validatePlayer(player);
        return (player << 6) | (color << 3) | HINT_COLOR;
    }

    public static int hintNumber(int player, int number) {
        Card.validateNumber(number);
        validatePlayer(player);
        return (player << 6) | (number << 3) | HINT_NUMBER;
    }

    public static int getType(int move) {
        return move & 0b11;
    }

    /**
     * Only for discard and play moves
     */
    public static int getPosition(int move) {
        return move >> 2;
    }

    /**
     * Only for hint moves
     */
    public static int getHintPlayer(int move) {
        return move >> 6;
    }

    /**
     * Only for hint moves
     */
    public static int getHintContent(int move) {
        return (move >> 3) & 0b111;
    }

    public static void validatePlayer(int player) {
        if (player < 0 || player >= 5) {
            throw new IllegalArgumentException("bad player");
        }
    }

    public static void validate(int move) {
        switch (getType(move)) {
            case DISCARD:
            case PLAY:
                Hand.validatePosition(getPosition(move));
                break;
            case HINT_COLOR:
                validatePlayer(getHintPlayer(move));
                Card.validateColor(getHintContent(move));
                break;
            case HINT_NUMBER:
                validatePlayer(getHintPlayer(move));
                Card.validateNumber(getHintContent(move));
                break;
            default:
                throw new IllegalArgumentException("bad type");
        }
    }
}
