package hanabi;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        runTrials();
    }

    private static void playSimpleGame() {
        Random random = new Random();
        random.setSeed(999695);
        Player[] players = {new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer()};
        GameState state = new GameState(false, 3, random);
        GameController controller = new GameController(state, players, true);
        controller.run();
        System.out.println(controller.getState().getScore());
        System.out.println(controller.getState().getTurns());
    }

    private static void runTrials() {
        final int TRIALS = 1000_000;
        Player[] players = {new CheatingPlayer(), new CheatingPlayer(), new CheatingPlayer()};
        int[] counts = new int[26];
        Random random = new Random();
        for (int i = 0; i < TRIALS; i++) {
            random.setSeed(i);
            GameState state = new GameState(false, 3, random);
            GameController controller = new GameController(state, players, false);
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
                System.out.printf("%d: %.2f%n", i, counts[i] * 100d / TRIALS);
                sum += i * counts[i];
            }
        }
        System.out.printf("mean: %.2f%n", sum / TRIALS);
    }
}
