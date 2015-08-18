package hanabi;

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
        while (!state.isFinished()) {
            int move = players[state.getCurrentPlayer()].getMove(state);
            state.applyMove(move);
            if (log) {
                System.out.println(state);
            }
            // TODO implement broadcasting
        }
    }

    public GameState getState() {
        return state;
    }
}
