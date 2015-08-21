package hanabi.gui;

import hanabi.GameState;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public class HanabiUI extends JComponent {
    private final JLabel turn, deck, score;
    private final TableauUI tableau;
    private final TokensUI hints, lives;
    private final HandsUI hands;
    private final CardMultiSetUI discard;

    public HanabiUI() {
        setLayout(new BorderLayout());

        turn = new JLabel("Turn 1337");
        turn.setHorizontalAlignment(SwingConstants.CENTER);
        turn.setFont(new Font(turn.getFont().getName(), Font.PLAIN, 50));
        turn.setOpaque(true);
        add(turn, BorderLayout.NORTH);

        tableau = new TableauUI(false);
        add(wrap(tableau, "Tableau"));

        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        add(bottom, BorderLayout.SOUTH);

        hands = new HandsUI();
        bottom.add(wrap(hands, "Hands"));

        hints = new TokensUI(4, 2, 8, new Color(114, 198, 255), new Color(32, 84, 255));
        bottom.add(wrap(hints, "Hints"));

        lives = new TokensUI(3, 1, 3, Color.BLACK, Color.RED);
        bottom.add(wrap(lives, "Lives"));

        deck = new JLabel("60");
        deck.setFont(new Font(deck.getFont().getName(), Font.PLAIN, 80));
        deck.setPreferredSize(new Dimension(125, 110));
        deck.setHorizontalAlignment(SwingConstants.CENTER);
        bottom.add(wrap(deck, "Deck"));

        score = new JLabel("25");
        score.setFont(new Font(deck.getFont().getName(), Font.BOLD, 80));
        score.setPreferredSize(new Dimension(125, 110));
        score.setHorizontalAlignment(SwingConstants.CENTER);
        score.setForeground(Color.BLUE);
        bottom.add(wrap(score, "Score"));

        discard = new CardMultiSetUI(false);
        bottom.add(wrap(discard, "Discard"));
    }

    private static JPanel wrap(JComponent component, String title) {
        JPanel panel = new JPanel();
        panel.add(component);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    public void update(GameState state) {
        String turnText = "Turn " + (state.getTurns() + 1);
        if (state.isFinished()) {
            turnText += " - GAME OVER";
        }
        turn.setText(turnText);

        score.setText(String.valueOf(state.getScore()));
        deck.setText(String.valueOf(state.getDeckSize()));

        tableau.update(state.getTableau());

        hints.setTokens(state.getHints());
        lives.setTokens(state.getLives());

        hands.update(state);
        discard.update(state.getDiscard());
    }
}
