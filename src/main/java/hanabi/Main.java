package hanabi;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        runTrials();
        //playSimpleGame();
    }

    /**
     * Play a single game
     */
    private static void playSimpleGame() {
        Random random = new Random();
        random.setSeed(999695);
        GameState state = new GameState(false, 4, random);
        GameController controller = new GameController(state, () -> {
            AbstractPlayer p = new HumanStylePlayer();
            p.setLoggingEnabled(true);
            return p;
        }, true);
        controller.run();
        System.out.println(controller.getState().getScore());
        System.out.println(controller.getState().getTurns());
    }

    /**
     * Play a lot of games and report statistics
     */
    private static void runTrials() {
        final int TRIALS = 11_000;
        StatisticsCollector collector = new StatisticsCollector();
        collector.run(
                SmartPlayer::new,
                rnd -> new GameState(false, 4, rnd),
                TRIALS
        );
        collector.getDiscardHist().dump(1.96, System.out);
        collector.getDiscardFiveHist().dump(1.96, System.out);
        collector.getScoreHist().dump(1.96, System.out);
    }
}
