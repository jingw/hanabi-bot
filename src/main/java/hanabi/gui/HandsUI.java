package hanabi.gui;

import hanabi.Card;
import hanabi.GameState;
import hanabi.Hand;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

public class HandsUI extends JComponent {
    public static final int CARD_WIDTH = 56;
    public static final int CARD_HEIGHT = 87;
    private static final double SCALE = 0.3;
    private static final int BORDER = 1;
    private final JLabel[][] labels;
    private final JLabel[] currentPlayer;
    private final JLabel[] playerLabel;

    public HandsUI() {
        setLayout(new GridLayout(5, 5, 2, 3));
        labels = new JLabel[5][5];
        currentPlayer = new JLabel[5];
        playerLabel = new JLabel[5];
        for (int p = 0; p < 5; p++) {
            currentPlayer[p] = new JLabel("*");
            currentPlayer[p].setHorizontalAlignment(SwingConstants.CENTER);
            add(currentPlayer[p]);
            playerLabel[p] = new JLabel(String.valueOf(p + 1));
            playerLabel[p].setHorizontalAlignment(SwingConstants.CENTER);
            add(playerLabel[p]);

            for (int i = 0; i < 5; i++) {
                JLabel l = new JLabel(String.valueOf(i + 1));
                labels[p][i] = l;
                l.setPreferredSize(new Dimension((int) (CARD_WIDTH * SCALE), (int) (CARD_HEIGHT * SCALE)));
                l.setBackground(TableauUI.COLORS[p]);
                l.setOpaque(true);
                //l.setFont(new Font(l.getFont().getName(), Font.PLAIN, (int) (SCALE * 60)));
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(BORDER, BORDER, BORDER, BORDER, Color.BLACK));
                add(l);
            }
        }
    }

    public void update(GameState state) {
        for (int p = 0; p < state.getNumPlayers(); p++) {
            currentPlayer[p].setVisible(state.getCurrentPlayer() == p);
            playerLabel[p].setVisible(true);
            int hand = state.getHand(p);
            for (int i = 0; i < 5; i++) {
                if (i < Hand.getSize(hand)) {
                    int card = Hand.getCard(hand, i);
                    labels[p][i].setText(String.valueOf(Card.getNumber(card) + 1));
                    labels[p][i].setBackground(TableauUI.COLORS[Card.getColor(card)]);
                    labels[p][i].setVisible(true);
                } else {
                    labels[p][i].setVisible(false);
                }
            }
        }
        for (int p = state.getNumPlayers(); p < 5; p++) {
            currentPlayer[p].setVisible(false);
            playerLabel[p].setVisible(false);
            for (int i = 0; i < 5; i++) {
                labels[p][i].setVisible(false);
            }
        }
    }
}
