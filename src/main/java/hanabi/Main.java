package hanabi;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        runTrials();
//        playSimpleGame();
    }

    /**
     * Play a single game
     */
    private static void playSimpleGame() {
        Random random = new Random();
        random.setSeed(999695);
        GameState state = new GameState(false, 5, random);
        GameController controller = new GameController(state, SmartPlayer::new, true);
        controller.run();
        System.out.println(controller.getState().getScore());
        System.out.println(controller.getState().getTurns());
    }

    /**
     * Play a lot of games and report statistics
     */
    private static void runTrials() {
        final int TRIALS = 11_000;
        Histogram hist = StatisticsCollector.run(
                SmartPlayer::new,
                rnd -> new GameState(false, 4, rnd),
                TRIALS
        );
        hist.dump(1.96, System.out);
    }
}
