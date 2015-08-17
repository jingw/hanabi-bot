package hanabi;

public class GameController {
    private GameState state;
    private Player[] players;

    public GameController(GameState state, Player[] players) {
        this.state = state;
        this.players = players;
    }

    public void run() {
        System.out.println(state);
        while (!state.isFinished()) {
            int move = players[state.getCurrentPlayer()].getMove(state);
            state.applyMove(move);
            System.out.println(state);
            // TODO implement broadcasting
        }
    }

    public GameState getState() {
        return state;
    }
}
