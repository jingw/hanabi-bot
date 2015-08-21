package hanabi.gui;

import hanabi.AbstractPlayer;
import hanabi.GameController;
import hanabi.GameState;
import hanabi.HumanStylePlayer;
import hanabi.RandomUtil;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.lang.reflect.InvocationTargetException;

public class GUI {
    private static final int MOVE_INTERVAL = 100;
    private static final int GAME_INTERVAL = 3000;

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        HanabiUI hanabi = new HanabiUI();

        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("Hanabi bot");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setContentPane(hanabi);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        while (true) {
            GameState state = new GameState(false, 4, RandomUtil.INSTANCE);
            GameController controller = new GameController(state, () -> {
                AbstractPlayer p = new HumanStylePlayer();
                p.setLoggingEnabled(true);
                return p;
            }, true);

            controller.setTurnHook((move) -> {
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        hanabi.update(state);
                    });
                    Thread.sleep(MOVE_INTERVAL);
                } catch (InterruptedException | InvocationTargetException e) {
                    throw new AssertionError(e);
                }
            });
            SwingUtilities.invokeAndWait(() -> {
                hanabi.update(state);
            });
            controller.run();
            Thread.sleep(GAME_INTERVAL);
        }
    }
}
