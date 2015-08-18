package hanabi;

/**
 * Runs the game by asking each player for move, applying it, and broadcasting the result. Stops
 * when the game is over.
 */
public class GameController {
    private GameState state;
    private Player[] players;
    private boolean log;

    public GameController(GameState state, Player[] players, boolean log) {
        this.state = state;
        this.players = players;
        this.log = log;
    }

    public void run() {
        if (log) {
            System.out.println(state);
        }
        int numPlayers = players.length;
        for (int i = 0; i < numPlayers; ++i) {
            players[i].notifyGameStarted(new GameStateView(state, i), i);
        }
        while (!state.isFinished()) {
            int player = state.getCurrentPlayer();
            int hand = state.getHand(player);
            int move = players[player].getMove();
            int type = Move.getType(move);
            int removedCard;
            switch (type) {
                case Move.DISCARD:
                case Move.PLAY:
                    removedCard = Hand.getCard(hand, Move.getPosition(move));
                    if (log) {
                        String verb = type == Move.DISCARD ? "discarded" : "played";
                        System.out.printf("%d %s %s%n", player, verb, Card.toString(removedCard));
                    }
                    break;
                case Move.HINT_COLOR:
                case Move.HINT_NUMBER:
                    removedCard = Card.NULL;
                    if (log) {
                        int target = Move.getHintPlayer(move);
                        int content = Move.getHintContent(move);
                        String what = type == Move.HINT_COLOR ? "color" : "number";
                        String contentStr = type == Move.HINT_COLOR ? "" + Card.colorToChar(content) : "" + content;
                        System.out.printf("%d hinted %d %s %s%n", player, target, what, contentStr);
                    }
                    break;
                default:
                    throw new AssertionError();
            }
            int result = state.applyMove(move);
            if (log) {
                System.out.println(state);
            }
            // We only really need to broadcast hints (the rest can be guessed from the game state), but broadcast the
            // whole log for convenience.
            switch (type) {
                case Move.DISCARD:
                    for (Player p : players) {
                        p.notifyDiscard(removedCard, Move.getPosition(move), player);
                        p.notifyDraw(p == players[player] ? Card.NULL : result, player);
                    }
                    break;
                case Move.PLAY:
                    for (Player p : players) {
                        p.notifyPlay(removedCard, Move.getPosition(move), player);
                        p.notifyDraw(p == players[player] ? Card.NULL : result, player);
                    }
                    break;
                case Move.HINT_COLOR: {
                    int color = Move.getHintContent(move);
                    int match = Hand.matchCardsColor(hand, color);
                    for (Player p : players) {
                        p.notifyHintNumber(Move.getHintPlayer(move), player, color, match);
                    }
                    break;
                }
                case Move.HINT_NUMBER: {
                    int number = Move.getHintContent(move);
                    int match = Hand.matchCardsColor(hand, number);
                    for (Player p : players) {
                        p.notifyHintNumber(Move.getHintPlayer(move), player, number, match);
                    }
                    break;
                }
            }
        }
    }

    public GameState getState() {
        return state;
    }
}
