package hanabi.gui;

import hanabi.Card;
import hanabi.Tableau;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class TableauUI extends JComponent {
    public static final Color[] COLORS = {
            Color.WHITE,
            new Color(88, 144, 255), // blue
            new Color(0, 150, 0), // green
            Color.YELLOW,
            new Color(230, 23, 41), // red
    };
    // ratio of actual card
    private static final int CARD_WIDTH = 56;
    private static final int CARD_HEIGHT = 87;
    private static final double SCALE = 2.5;
    private static final int BORDER = (int) (SCALE * 2);

    private final JLabel[][] labels;

    public TableauUI(boolean rainbow) {
        setLayout(null);
        // TODO rainbow later
        labels = new JLabel[Card.NUM_COLORS - 1][Card.NUM_NUMBERS];
        for (int color = 0; color < Card.NUM_COLORS - 1; color++) {
            for (int n = Card.NUM_NUMBERS - 1; n >= 0; n--) {
                double x = SCALE * (CARD_WIDTH * 1.4 * color + CARD_WIDTH * 0.07 * n);
                double y = CARD_HEIGHT * SCALE * 0.1 * n;
                JLabel label = new JLabel(String.valueOf(n + 1));
                label.setSize(new Dimension((int) (CARD_WIDTH * SCALE), (int) (CARD_HEIGHT * SCALE)));
                label.setBackground(COLORS[color]);
                label.setOpaque(true);
                label.setFont(new Font(label.getFont().getName(), Font.PLAIN, (int) (SCALE * 60)));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(BORDER, BORDER, BORDER, BORDER, Color.BLACK));
                label.setLocation((int) x, (int) y);
                add(label);
                labels[color][n] = label;
            }
        }
        double maxX = SCALE * (CARD_WIDTH * 1.4 * 4 + CARD_WIDTH * 0.07 * (Card.NUM_NUMBERS - 1)) + CARD_WIDTH * SCALE;
        double maxY = CARD_HEIGHT * SCALE * 0.1 * (Card.NUM_NUMBERS - 1) + CARD_HEIGHT * SCALE;
        setPreferredSize(new Dimension((int) maxX, (int) maxY));
    }

    public void update(int tableau) {
        for (int c = 0; c < 5; c++) {
            for (int n = 0; n < 5; n++) {
                labels[c][n].setVisible(n < Tableau.getCount(tableau, c));
            }
        }
    }
}
