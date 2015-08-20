package hanabi;

public abstract class AbstractPlayer implements Player {
    protected GameStateView state;
    protected int position;
    protected boolean log;

    public void setLoggingEnabled(boolean log) {
        this.log = log;
    }

    public boolean isLoggingEnabled() {
        return log;
    }

    protected void log(String msg, Object... args) {
        if (log) {
            System.out.printf("[p%d] ", position);
            System.out.printf(msg + "%n", args);
        }
    }

    @Override
    public void notifyGameStarted(GameStateView state, int position) {
        this.state = state;
        this.position = position;
    }

    @Override
    public void notifyHintColor(int targetPlayer, int sourcePlayer, int color, int which) {
    }

    @Override
    public void notifyHintNumber(int targetPlayer, int sourcePlayer, int number, int which) {
    }

    @Override
    public void notifyPlay(int card, int position, int sourcePlayer) {
    }

    @Override
    public void notifyDiscard(int card, int position, int sourcePlayer) {
    }

    @Override
    public void notifyDraw(int card, int sourcePlayer) {
    }
}
