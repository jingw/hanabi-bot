package hanabi;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StatisticsCollector {
    public static Histogram run(Supplier<Player> playerFactory,
                                Function<Random, GameState> stateFactory,
                                int trials) {
        return run(playerFactory, stateFactory, trials, x -> {
        });
    }

    public static Histogram run(Supplier<Player> playerFactory,
                                Function<Random, GameState> stateFactory,
                                int trials,
                                Consumer<GameState> finishHook) {
        Histogram hist = new Histogram(31);
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
            hist.increment(score);
            finishHook.accept(state);
        }
        return hist;
    }
}
