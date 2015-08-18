package hanabi;

/**
 * Always play the first card
 */
public class DumbPlayer implements Player {
    @Override
    public int getMove(GameState state) {
        return Move.play(0);
    }

    @Override
    public void notifyHintColor(int targetPlayer, int sourcePlayer, int color, int which) {
    }

    @Override
    public void notifyHintNumber(int targetPlayer, int sourcePlayer, int number, int which) {
    }

    @Override
    public void notifyPlay(int card, int position, int player) {
    }

    @Override
    public void notifyDiscard(int card, int position, int player) {
    }

    @Override
    public void notifyDraw(int card, int player) {
    }

    @Override
    public void notifyGameStarted(GameState state, int position) {
    }
}
