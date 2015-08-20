package hanabi;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Fuzz test all player implementations by running a ton of trials.
 */
public class PlayerFuzzTest {
    @Test
    public void testCheatingPlayer() {
        for (int nPlayers = 2; nPlayers <= 5; nPlayers++) {
            for (boolean rainbow : new boolean[]{true, false}) {
                final int N = nPlayers;
                StatisticsCollector sc = new StatisticsCollector();
                sc.run(
                        CheatingPlayer::new,
                        rnd -> new GameState(rainbow, N, rnd),
                        10_000,
                        state -> {
                            // it should be nearly impossible for the cheater to get so few points
                            Assert.assertTrue(state.getScore() > 10);
                            Assert.assertTrue(state.getTurns() > 10);
                            // the cheater should never waste a life
                            Assert.assertEquals(state.getLives(), GameState.MAX_LIVES);
                        }
                );
                Histogram hist = sc.getScoreHist();
                int maxScore = rainbow ? 30 : 25;
                Assert.assertTrue(hist.mean() > maxScore - 1);
            }
        }
    }

    @Test
    public void testDumbPlayer() {
        for (int nPlayers = 2; nPlayers <= 5; nPlayers++) {
            for (boolean rainbow : new boolean[]{true, false}) {
                final int N = nPlayers;
                StatisticsCollector sc = new StatisticsCollector();
                sc.run(
                        DumbPlayer::new,
                        rnd -> new GameState(rainbow, N, rnd),
                        10_000,
                        state -> {
                            // it should be nearly impossible for the dumb player to get any points
                            Assert.assertTrue(state.getScore() < 15);
                            Assert.assertTrue(state.getTurns() < 15);
                            // the dumb player should always end on lives
                            Assert.assertEquals(state.getLives(), 0);
                        }
                );
                Histogram hist = sc.getScoreHist();
                Assert.assertTrue(hist.mean() < 10);
            }
        }
    }

    @Test
    public void testSmartPlayer() {
        for (int nPlayers = 2; nPlayers <= 5; nPlayers++) {
            for (boolean rainbow : new boolean[]{true, false}) {
                final int N = nPlayers;
                StatisticsCollector sc = new StatisticsCollector();
                sc.run(
                        SmartPlayer::new,
                        rnd -> new GameState(rainbow, N, rnd),
                        10_000
                );
            }
        }
    }

    @Test
    public void testHumanStylePlayer() {
        for (int nPlayers = 2; nPlayers <= 5; nPlayers++) {
            for (boolean rainbow : new boolean[]{true, false}) {
                final int N = nPlayers;
                StatisticsCollector sc = new StatisticsCollector();
                sc.run(
                        HumanStylePlayer::new,
                        rnd -> new GameState(rainbow, N, rnd),
                        10_000
                );
            }
        }
    }
}
