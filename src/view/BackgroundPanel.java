package view;

import javax.swing.*;
import java.awt.*;

public class BackgroundPanel extends JPanel {
    private final Image bg;

    public BackgroundPanel(Image bg) {
        this.bg = bg;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bg == null) return;

        int imgW = bg.getWidth(null);
        int imgH = bg.getHeight(null);
        if (imgW <= 0 || imgH <= 0) return;

        int panelW = getWidth();
        int panelH = getHeight();

        // scale to cover the whole panel (like “cover” in CSS)
        double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);

        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);

        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;

        g.drawImage(bg, x, y, drawW, drawH, this);
    }
}
