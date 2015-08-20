package hanabi;

public class GameStateView {
    private GameState state;
    private int viewingPlayer;

    public GameStateView(GameState state, int player) {
        this.state = state;
        this.viewingPlayer = player;
    }

    public boolean isFinished() {
        return state.isFinished();
    }

    public int getScore() {
        return state.getScore();
    }

    public int getCurrentPlayer() {
        return state.getCurrentPlayer();
    }

    public int getNumPlayers() {
        return state.getNumPlayers();
    }

    public int getHints() {
        return state.getHints();
    }

    public int getDeckSize() {
        return state.getDeckSize();
    }

    public int getHand(int player) {
        if (player == viewingPlayer) {
            throw new IllegalStateException("cannot get hand of current player");
        }
        return state.getHand(player);
    }

    public int getHandUnsafe(int player) {
        return state.getHand(player);
    }

    public int getHandSize(int player) {
        return Hand.getSize(state.getHand(player));
    }

    public int getMyHandSize() {
        return getHandSize(viewingPlayer);
    }

    public int getTableau() {
        return state.getTableau();
    }

    public int getLives() {
        return state.getLives();
    }

    public long getDiscard() {
        return state.getDiscard();
    }

    public int getTurnsLeft() {
        return state.getTurnsLeft();
    }

    public boolean isRainbow() {
        return state.isRainbow();
    }
}
