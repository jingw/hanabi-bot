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
        Random random = new Random();
        for (int nPlayers = 2; nPlayers <= 5; nPlayers++) {
            for (boolean rainbow : new boolean[]{true, false}) {
                for (int trial = 0; trial < 10_000; trial++) {
                    Player[] players = new Player[nPlayers];
                    for (int i = 0; i < nPlayers; i++) {
                        players[i] = new CheatingPlayer();
                    }
                    random.setSeed(trial);
                    GameState state = new GameState(rainbow, nPlayers, random);
                    GameController controller = new GameController(state, players, false);
                    controller.run();
                    // it should be nearly impossible for the cheater to get so few points
                    Assert.assertTrue(controller.getState().getScore() > 10);
                    Assert.assertTrue(controller.getState().getTurns() > 10);
                    // the cheater should never waste a life
                    Assert.assertEquals(controller.getState().getLives(), GameState.MAX_LIVES);
                }
            }
        }
    }

    @Test
    public void testDumbPlayer() {
        Random random = new Random();
        for (int nPlayers = 2; nPlayers <= 5; nPlayers++) {
            for (boolean rainbow : new boolean[]{true, false}) {
                for (int trial = 0; trial < 10_000; trial++) {
                    Player[] players = new Player[nPlayers];
                    for (int i = 0; i < nPlayers; i++) {
                        players[i] = new DumbPlayer();
                    }
                    random.setSeed(trial);
                    GameState state = new GameState(rainbow, nPlayers, random);
                    GameController controller = new GameController(state, players, false);
                    controller.run();
                    // it should be nearly impossible for the dumb player to get any points
                    Assert.assertTrue(controller.getState().getScore() < 15);
                    Assert.assertTrue(controller.getState().getTurns() < 15);
                    // the dumb player should always end on lives
                    Assert.assertEquals(controller.getState().getLives(), 0);
                }
            }
        }
    }

    @Test
    public void testSmartPlayer() {
        Random random = new Random();
        for (int nPlayers = 2; nPlayers <= 5; nPlayers++) {
            for (boolean rainbow : new boolean[]{true, false}) {
                for (int trial = 0; trial < 10_000; trial++) {
                    Player[] players = new Player[nPlayers];
                    for (int i = 0; i < nPlayers; i++) {
                        players[i] = new SmartPlayer();
                    }
                    random.setSeed(trial);
                    GameState state = new GameState(rainbow, nPlayers, random);
                    GameController controller = new GameController(state, players, false);
                    controller.run();
                }
            }
        }
    }
}
