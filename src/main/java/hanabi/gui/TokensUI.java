package hanabi.gui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class TokensUI extends JComponent {
    private int tokens;
    private final int width, height;
    private final Color fill, border;

    public TokensUI(int width, int height, int tokens, Color fill, Color border) {
        this.width = width;
        this.height = height;
        this.tokens = tokens;
        this.fill = fill;
        this.border = border;
        setPreferredSize(new Dimension(32 * width + 3, 32 * height + 3));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < tokens; i++) {
            int r = i / 4;
            int c = i % 4;
            int x = c * 32 + 3;
            int y = r * 32 + 3;

            g2.setColor(fill);
            g2.fillOval(x, y, 25, 25);

            g2.setColor(border);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(x, y, 25, 25);
        }
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
        this.repaint();
    }
}
