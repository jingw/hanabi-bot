package hanabi.gui;

import hanabi.Card;
import hanabi.CardMultiSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;

public class CardMultiSetUI extends JComponent {
    public static final int CARD_WIDTH = 56;
    public static final int CARD_HEIGHT = 87;
    private static final double SCALE = 0.3;
    private static final int BORDER = 1;
    private JLabel[][][] labels;

    public CardMultiSetUI(boolean rainbow) {
        labels = new JLabel[5][5][];
        for (int c = 0; c < 5; c++) {
            int sum = 0;
            for (int n = 0; n < 5; n++) {
                labels[c][n] = new JLabel[Card.NUM_COUNTS[n]];
                for (int i = 0; i < labels[c][n].length; i++) {
                    JLabel l = new JLabel(String.valueOf(n + 1));
                    labels[c][n][i] = l;
                    double x = SCALE * CARD_WIDTH * 1.1 * (sum + n * 0.4);
                    double y = CARD_HEIGHT * SCALE * 1.1 * c;
                    l.setSize(new Dimension((int) (CARD_WIDTH * SCALE), (int) (CARD_HEIGHT * SCALE)));
                    l.setBackground(TableauUI.COLORS[c]);
                    l.setOpaque(true);
                    //l.setFont(new Font(l.getFont().getName(), Font.PLAIN, (int) (SCALE * 60)));
                    l.setHorizontalAlignment(JLabel.CENTER);
                    l.setBorder(BorderFactory.createMatteBorder(BORDER, BORDER, BORDER, BORDER, Color.BLACK));
                    l.setLocation((int) x, (int) y);
                    add(l);
                    sum++;
                }
            }
        }
        double maxx = SCALE * (CARD_WIDTH * 1.1 * (9 + 4 * 0.4) + CARD_WIDTH);
        double maxy = SCALE * (CARD_HEIGHT * 1.1 * 4 + CARD_HEIGHT);
        setPreferredSize(new Dimension((int) maxx, (int) maxy));
    }

    public void update(long discard) {
        for (int c = 0; c < 5; c++) {
            for (int n = 0; n < 5; n++) {
                int count = CardMultiSet.getCount(discard, Card.create(c, n));
                for (int i = 0; i < labels[c][n].length; i++) {
                    labels[c][n][i].setVisible(i < count);
                }
            }
        }
    }
}
