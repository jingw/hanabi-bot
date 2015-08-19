package hanabi;

public interface Player {
    int getMove();

    /**
     * Called when the game starts.
     * @param state TODO
     * @param position the position of player (0 goes first)
     */
    void notifyGameStarted(GameStateView state, int position);
    /**
     * Called when any hint is given, including by this player
     * @param which bit mask in the same order as hand.
     */
    void notifyHintColor(int targetPlayer, int sourcePlayer, int color, int which);

    /**
     * Called when any hint is given, including by this player
     */
    void notifyHintNumber(int targetPlayer, int sourcePlayer, int number, int which);

    /**
     * Called when a player plays a card, including this player
     */
    void notifyPlay(int card, int position, int sourcePlayer);

    /**
     * Called when a player discards a card, including this player
     */
    void notifyDiscard(int card, int position, int sourcePlayer);

    /**
     * Called when a player draws a card. This will be Card.NULL if you are the one drawing. Not
     * called when the deck is empty and nothing is actually drawn.
     */
    void notifyDraw(int card, int sourcePlayer);
}
