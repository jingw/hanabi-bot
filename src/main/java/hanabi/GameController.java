package hanabi;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Runs the game by asking each player for move, applying it, and broadcasting the result. Stops
 * when the game is over.
 */
public class GameController {
    private GameState state;
    private Player[] players;
    private boolean log;
    private Consumer<Integer> turnHook;

    private static Player[] makePlayers(int n, Supplier<Player> playerFactory) {
        Player[] players = new Player[n];
        for (int p = 0; p < n; p++) {
            players[p] = playerFactory.get();
        }
        return players;
    }

    public GameController(GameState state, Supplier<Player> playerFactory, boolean log) {
        this(state, makePlayers(state.getNumPlayers(), playerFactory), log);
    }

    public GameController(GameState state, Player[] players, boolean log) {
        if (state.getNumPlayers() != players.length) {
            throw new IllegalArgumentException("num players mismatch");
        }
        this.state = state;
        this.players = players;
        this.log = log;
    }

    public void setTurnHook(Consumer<Integer> turnHook) {
        this.turnHook = turnHook;
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
                        if (result != Card.NULL) {
                            p.notifyDraw(p == players[player] ? Card.NULL : result, player);
                        }
                    }
                    break;
                case Move.PLAY:
                    for (Player p : players) {
                        p.notifyPlay(removedCard, Move.getPosition(move), player);
                        if (result != Card.NULL) {
                            p.notifyDraw(p == players[player] ? Card.NULL : result, player);
                        }
                    }
                    break;
                case Move.HINT_COLOR: {
                    int color = Move.getHintContent(move);
                    int target = Move.getHintPlayer(move);
                    int match = Hand.matchCardsColor(state.getHand(target), color);
                    for (Player p : players) {
                        p.notifyHintColor(target, player, color, match);
                    }
                    break;
                }
                case Move.HINT_NUMBER: {
                    int number = Move.getHintContent(move);
                    int target = Move.getHintPlayer(move);
                    int match = Hand.matchCardsNumber(state.getHand(target), number);
                    for (Player p : players) {
                        p.notifyHintNumber(target, player, number, match);
                    }
                    break;
                }
                default:
                    throw new AssertionError();
            }
            if (turnHook != null)
                turnHook.accept(move);
        }
    }

    public GameState getState() {
        return state;
    }
}
