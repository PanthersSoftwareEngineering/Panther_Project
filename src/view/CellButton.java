package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CellButton extends JButton {

    private Color base;
    private Color hover;
    private Color pressed;
    private boolean rounded = true;

    public CellButton(String text, Color base) {
        super(text);
        setBaseColor(base);

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false); // we paint ourselves
        setOpaque(false);

        setFont(new Font("SansSerif", Font.BOLD, 16));
        setMargin(new Insets(0, 0, 0, 0));
        setHorizontalAlignment(SwingConstants.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (isEnabled()) repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                if (isEnabled()) repaint();
            }
            @Override public void mousePressed(MouseEvent e) {
                if (isEnabled()) repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (isEnabled()) repaint();
            }
        });
    }

    public void setBaseColor(Color c) {
        this.base = c;
        // auto derive nicer hover/pressed
        this.hover   = brighten(c, 0.10f);
        this.pressed = darken(c, 0.10f);
        repaint();
    }

    public Color getBaseColor() { return base; }

    public void setRounded(boolean rounded) {
        this.rounded = rounded;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        boolean hover   = getModel().isRollover();
        boolean pressed = getModel().isPressed();
        boolean enabled = isEnabled();

        Color fill = base;
        if (!enabled) {
            fill = new Color(120,120,120,120);
        } else if (pressed) {
            fill = darken(base, 0.15f);
        } else if (hover) {
            fill = brighten(base, 0.20f);
        }

        // ==========================
        // 🌟 GLOW EFFECT
        // ==========================
        if (enabled) {
            Color glow = brighten(fill, hover ? 0.45f : 0.30f);
            g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 90));
            g2.fillRoundRect(-2, -2, w + 4, h + 4, 20, 20);
        }

        // main body
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w - 4, h - 4, 16, 16);

        // border
        g2.setColor(new Color(255,255,255, hover ? 160 : 100));
        g2.drawRoundRect(0, 0, w - 4, h - 4, 16, 16);

        g2.dispose();

        super.paintComponent(g);
    }


    // keep default text rendering but no background
    @Override
    public boolean isContentAreaFilled() {
        return false;
    }

    private static Color brighten(Color c, float amount) {
        int r = (int) Math.min(255, c.getRed()   + 255 * amount);
        int g = (int) Math.min(255, c.getGreen() + 255 * amount);
        int b = (int) Math.min(255, c.getBlue()  + 255 * amount);
        return new Color(r, g, b, c.getAlpha());
    }

    private static Color darken(Color c, float amount) {
        int r = (int) Math.max(0, c.getRed()   - 255 * amount);
        int g = (int) Math.max(0, c.getGreen() - 255 * amount);
        int b = (int) Math.max(0, c.getBlue()  - 255 * amount);
        return new Color(r, g, b, c.getAlpha());
    }
}
