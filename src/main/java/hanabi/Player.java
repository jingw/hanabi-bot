package hanabi;

public interface Player {
    // TODO pass a view of the state
    int getMove(GameState state);

    /**
     * Called when the game starts.
     * @param position the position of player (0 goes first)
     */
    void notifyGameStarted(GameState state, int position);
    /**
     * Called when any hint is given, including by this player
     * @param state TODO
     * @param which bit mask in the same order as hand.
     */
    void notifyHintColor(GameState state, int targetPlayer, int sourcePlayer, int color, int which);

    /**
     * Called when any hint is given, including by this player
     * @param state TODO
     */
    void notifyHintNumber(GameState state, int targetPlayer, int sourcePlayer, int number, int which);

    /**
     * Called when a player plays a card, including this player
     * @param state TODO
     */
    void notifyPlay(GameState state, int card, int position, int sourcePlayer);

    /**
     * Called when a player discards a card, including this player
     * @param state TODO
     */
    void notifyDiscard(GameState state, int card, int position, int sourcePlayer);

    /**
     * Called when a player draws a card. This will be Card.NULL if you are the one drawing.
     * @param state TODO
     */
    void notifyDraw(GameState state, int card, int sourcePlayer);
}
