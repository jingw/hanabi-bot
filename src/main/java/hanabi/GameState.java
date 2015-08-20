package hanabi;

import java.util.Random;

/**
 * Stores everything about the current state of the game.
 */
public final class GameState {
    public static final int MAX_HINTS = 8;
    public static final int MAX_LIVES = 3;

    private int[] deck;
    private int deckIndex;
    /**
     * how many hints remain
     */
    private int hints = MAX_HINTS;
    /**
     * how many lives remain
     */
    private int lives = MAX_LIVES;
    /**
     * each player's cards
     */
    private int[] hands;
    /**
     * who's turn is next
     */
    private int currentPlayer;
    /**
     * who drew the last card
     */
    private int endingPlayer = -1;
    /**
     * bit vector in the same order as ALL_CARDS
     */
    private long discard = CardMultiSet.EMPTY;
    /**
     * played cards, stored using 3 bits per color
     */
    private int tableau = Tableau.EMPTY;
    private boolean finished = false;
    private int turns = 0;

    /**
     * Create a fresh game with the given number of players.
     */
    public GameState(boolean rainbow, int players, Random random) {
        deck = Card.makeDeck(rainbow);
        RandomUtil.shuffle(deck, random);
        deckIndex = deck.length - 1;
        dealInitialCards(players);
    }

    private void dealInitialCards(int players) {
        if (players < 2 || players > 5)
            throw new IllegalArgumentException("players out of range");
        hands = new int[players];
        int handSize = players < 4 ? 5 : 4;
        for (int i = 0; i < players; i++) {
            hands[i] = Hand.EMPTY;
            for (int j = 0; j < handSize; j++) {
                hands[i] = Hand.append(hands[i], deck[deckIndex--]);
            }
        }
    }

    /**
     * Create a game from a custom starting point
     */
    public GameState(int[] deck, int hints, int lives, int[] hands, int currentPlayer, int endingPlayer, long discard, int tableau) {
        // TODO validation
        this.deck = deck;
        this.deckIndex = deck.length - 1;
        this.hints = hints;
        this.lives = lives;
        this.hands = hands;
        this.currentPlayer = currentPlayer;
        this.endingPlayer = endingPlayer;
        this.discard = discard;
        this.tableau = tableau;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getScore() {
        int score = 0;
        for (int color = 0; color < Card.NUM_COLORS; color++) {
            score += Tableau.getCount(tableau, color);
        }
        return score;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getNumPlayers() {
        return hands.length;
    }

    /**
     * Apply the given move to the game state. If the move is a play or removeCard, return the drawn card.
     * The move is assumed to come from the current player.
     */
    public int applyMove(int move) {
        if (finished) {
            throw new IllegalStateException("already finished");
        }

        Move.validate(move);
        int result;
        int type = Move.getType(move);
        switch (type) {
            case Move.DISCARD:
                result = applyDiscard(move);
                break;
            case Move.PLAY:
                result = applyPlay(move);
                break;
            case Move.HINT_COLOR:
            case Move.HINT_NUMBER:
                if (hints == 0) {
                    throw new IllegalStateException("no hints");
                }
                int targetPlayer = Move.getHintPlayer(move);
                if (targetPlayer == currentPlayer) {
                    throw new IllegalArgumentException("cannot hint yourself");
                }
                int content = Move.getHintContent(move);
                if (type == Move.HINT_COLOR) {
                    if (Hand.matchCardsColor(hands[targetPlayer], content) == 0) {
                        throw new IllegalArgumentException("no matching color");
                    }
                } else {
                    if (Hand.matchCardsNumber(hands[targetPlayer], content) == 0) {
                        throw new IllegalArgumentException("no matching number");
                    }
                }
                hints--;
                result = Card.NULL;
                break;
            default:
                throw new AssertionError();
        }
        endOfTurnCleanup();
        return result;
    }

    private void endOfTurnCleanup() {
        if (deckIndex == -1) {
            // deck is empty
            if (endingPlayer == currentPlayer) {
                // we've already gone around, and the player who drew the last card just got a chance to play
                finished = true;
            }
            if (endingPlayer == -1) {
                // current player just drew the last card
                endingPlayer = currentPlayer;
            }
        }

        currentPlayer = (currentPlayer + 1) % hands.length;
        turns++;
    }

    private int applyDiscard(int move) {
        int pos = Move.getPosition(move);
        moveToDiscard(pos);
        incrementHints();
        return drawCard();
    }

    /**
     * Move the card in the given position to the discard
     */
    private void moveToDiscard(int pos) {
        int card = Hand.getCard(hands[currentPlayer], pos);
        discard = CardMultiSet.increment(discard, card);
        removeCard(pos);
    }

    /**
     * Remove the card in the given position
     */
    private void removeCard(int pos) {
        hands[currentPlayer] = Hand.discard(hands[currentPlayer], pos);
    }

    private int drawCard() {
        if (deckIndex >= 0) {
            int newCard = deck[deckIndex--];
            hands[currentPlayer] = Hand.append(hands[currentPlayer], newCard);
            return newCard;
        } else {
            return Card.NULL;
        }
    }

    private int applyPlay(int move) {
        int pos = Move.getPosition(move);
        int card = Hand.getCard(hands[currentPlayer], pos);
        if (Tableau.isPlayable(tableau, card)) {
            removeCard(pos);
            int color = Card.getColor(card), num = Card.getNumber(card);
            // successful play
            tableau = Tableau.increment(tableau, color);
            if (num == Card.MAX_NUMBER) {
                // hint back for maxing a stack
                incrementHints();
            }
        } else {
            moveToDiscard(pos);
            lives--;
            if (lives == 0) {
                finished = true;
            }
        }
        return drawCard();
    }

    private void incrementHints() {
        if (hints != MAX_HINTS) {
            hints++;
        }
    }

    public int getHints() {
        return hints;
    }

    public int getDeckSize() {
        return deckIndex + 1;
    }

    public int getHand(int player) {
        return hands[player];
    }

    /**
     * Return the player that drew the last card, or -1 if that has not yet happened
     */
    public int getEndingPlayer() {
        return endingPlayer;
    }

    public int getTableau() {
        return tableau;
    }

    public int getLives() {
        return lives;
    }

    public long getDiscard() {
        return discard;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('{');
        result.append("hints=").append(hints);
        result.append(',');
        result.append("lives=").append(lives);
        result.append(',');
        result.append("currentPlayer=").append(currentPlayer);
        result.append(',');
        result.append("endingPlayer=").append(endingPlayer);
        result.append(',');
        result.append("finished=").append(finished);
        result.append(',');

        result.append("tableau=").append(Tableau.toString(tableau));
        result.append(',');

        result.append("hands=[");
        for (int i = 0; i < hands.length; i++) {
            if (i != 0) {
                result.append(',');
            }
            result.append(Hand.toString(hands[i]));
        }
        result.append("],");

        result.append("deck=[");
        for (int i = 0; i <= deckIndex; i++) {
            if (i != 0) {
                result.append(',');
            }
            result.append(Card.toString(deck[i]));
        }
        result.append("],");

        result.append("discard=").append(CardMultiSet.toString(discard));

        result.append('}');
        return result.toString();
    }

    public int getTurns() {
        return turns;
    }

    /**
     * If game end has been triggered, return the number of turns left. If the game has ended,
     * return zero. Otherwise, return Integer.MAX_VALUE.
     */
    public int getTurnsLeft() {
        if (endingPlayer == -1) {
            return Integer.MAX_VALUE;
        } else if (finished) {
            return 0;
        } else {
            return (endingPlayer - currentPlayer + hands.length) % hands.length + 1;
        }
    }
}
