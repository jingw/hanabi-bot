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
        Player[] cheatingPlayers = {new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer()};
        Player[] smartPlayers = {new SmartPlayer(), new SmartPlayer(), new SmartPlayer(), new SmartPlayer(), new SmartPlayer()};
        GameState state = new GameState(false, 5, random);
        GameController controller = new GameController(state, smartPlayers, true);
        controller.run();
        System.out.println(controller.getState().getScore());
        System.out.println(controller.getState().getTurns());
    }

    /**
     * Play a lot of games and report statistics
     */
    private static void runTrials() {
        final int TRIALS = 11_000;
        Player[] cheatingPlayers3 = {new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer()};
        Player[] cheatingPlayers5 = {new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer()};
        Player[] smartPlayers3 = {new SmartPlayer(), new SmartPlayer(), new SmartPlayer()};
        Player[] smartPlayers4 = {new SmartPlayer(), new SmartPlayer(), new SmartPlayer(), new SmartPlayer()};
        Player[] smartPlayers5 = {new SmartPlayer(), new SmartPlayer(), new SmartPlayer(), new SmartPlayer(), new SmartPlayer()};
        int[] counts = new int[26];
        Random random = new Random();
        for (int i = 0; i < TRIALS; i++) {
            random.setSeed(i);
            GameState state = new GameState(false, 4, random);
            GameController controller = new GameController(state, smartPlayers4, false);
            controller.run();
            int score = controller.getState().getScore();
            counts[score]++;
            /*if (score < 25) {
                System.out.println(i);
            }*/
        }

        double sum = 0;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                double p = (double) counts[i] / TRIALS;
                double margin = confidenceErrorMargin(p, TRIALS, 1.96);
                System.out.printf("%d: %.3f +- %.3f%n", i, p * 100, margin * 100);
                sum += i * counts[i];
            }
        }
        System.out.printf("mean: %.3f%n", sum / TRIALS);
    }

    private static double confidenceErrorMargin(double pObserved, int n, double z) {
        double stderr = Math.sqrt(pObserved * (1 - pObserved) / n);
        return stderr * z;
    }
}
