package hanabi.gui;

import hanabi.GameController;
import hanabi.GameState;
import hanabi.HumanStylePlayer;
import hanabi.RandomUtil;
import org.junit.Test;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

public class GUITest {
    /**
     * simple smoke test
     */
    @Test
    public void testUpdateLoop() throws InvocationTargetException, InterruptedException {
        HanabiUI hanabi = new HanabiUI();
        GameState state = new GameState(false, 4, RandomUtil.INSTANCE);
        GameController controller = new GameController(state, HumanStylePlayer::new, false);

        controller.setTurnHook((move) -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    hanabi.update(state);
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new AssertionError(e);
            }
        });
        SwingUtilities.invokeAndWait(() -> {
            hanabi.update(state);
        });
        controller.run();
    }
}
