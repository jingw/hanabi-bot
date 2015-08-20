package hanabi;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StatisticsCollector {
    private Histogram score_hist = new Histogram(31, "Scores");
    private Histogram discard_hist = new Histogram(31, "Bad discards");
    private Histogram discard_five_hist = new Histogram(31, "Bad discards of 5s");
    private Histogram lives_hist = new Histogram(31, "Remaining lives");
    private Histogram first_discard_hist
        = new Histogram(31, "Turn number of first bad discard", 5);
    private Histogram first_discard_cards_played_hist
        = new Histogram(31, "Cards played before first bad discard", 5);

    public Histogram getScoreHist() {
        return score_hist;
    }

    public Histogram getDiscardHist() {
        return discard_hist;
    }

    public Histogram getDiscardFiveHist() {
        return discard_five_hist;
    }

    public Histogram getLivesHist() {
        return lives_hist;
    }

    public Histogram getFirstBadDiscardHist() {
        return first_discard_hist;
    }

    public Histogram getFirstBadDiscardCardsPlayedHist() {
        return first_discard_cards_played_hist;
    }

    public void run(Supplier<Player> playerFactory,
                         Function<Random, GameState> stateFactory,
                         int trials) {
        run(playerFactory, stateFactory, trials, x -> {
        });
    }

    public void run(Supplier<Player> playerFactory,
                    Function<Random, GameState> stateFactory,
                    int trials,
                    Consumer<GameState> finishHook) {
        Random random = new Random();
        for (int i = 0; i < trials; i++) {
            random.setSeed(i);
            GameState state = stateFactory.apply(random);
            GameController controller = new GameController(state, playerFactory, false);
            try {
                controller.run();
            } catch (RuntimeException | AssertionError e) {
                throw new RuntimeException("Failed trial " + i + ", game state " + state, e);
            }
            int score = controller.getState().getScore();
            score_hist.increment(score);
            discard_hist.increment(controller.getState().overDiscards());
            discard_five_hist.increment(controller.getState().overDiscards(4));
            lives_hist.increment(controller.getState().getLives());
            if (controller.getState().getFirstBadDiscard() >= 0) {
                first_discard_hist.increment(controller.getState().getFirstBadDiscard());
                first_discard_cards_played_hist.increment(
                    controller.getState().getCardsPlayedAtFirstBadDiscard());
            }
            finishHook.accept(state);
        }
    }
}
