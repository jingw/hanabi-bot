package hanabi;

public interface Player {
    // TODO pass a view of the state
    int getMove(GameState state);

    /**
     * Called when any hint is given, including by this player
     */
    void notifyHint(int hint);

    /** Called when a player plays a card */
    void notifyPlay(int card, int position, int player);

    /** Called when a player discards a card */
    void notifyDiscard(int card, int position, int player);

    /** Called when a player draws a card */
    void notifyDraw(int card, int player);
}
