package view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class UIStyles {

    private UIStyles(){}

    // ---------- Fonts ----------
    public static final Font HUD_FONT       = new Font("SansSerif", Font.BOLD, 18);
    public static final Font HUD_FONT_SMALL = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font TITLE_FONT     = new Font("SansSerif", Font.BOLD, 22);

    // ---------- Base Theme Colors ----------
    public static final Color GOLD_TEXT = new Color(255, 215, 110);

    // ✅ Dynamic accent (what changes permanently via personalization)
    // Default is gold so your app stays the same until user changes it.
    public static Color ACCENT = GOLD_TEXT;

    // ---------- Colors ----------
    // Use ACCENT for HUD text so it changes with personalization
    public static Color HUD_TEXT  = ACCENT;

    // "chip" badge colors
    public static final Color CHIP_BG_ACTIVE_P1 = new Color(0, 120, 215, 180);  // blue
    public static final Color CHIP_BG_ACTIVE_P2 = new Color(46, 139, 87, 180);  // green
    public static final Color CHIP_TEXT = Color.WHITE;

    // translucent HUD panel
    public static final Color HUD_PANEL_BG = new Color(0, 0, 0, 110);

    // ---------- Theme API ----------
    public static void setAccent(Color c) {
        if (c == null) return;
        ACCENT = c;
        HUD_TEXT = ACCENT; // keep HUD synced
    }

    // ---------- Helpers ----------
    public static void styleHudLabel(JLabel lbl) {
        lbl.setForeground(HUD_TEXT);
        lbl.setFont(HUD_FONT);
    }

    public static void styleHudLabelSmall(JLabel lbl) {
        lbl.setForeground(HUD_TEXT);
        lbl.setFont(HUD_FONT_SMALL);
    }

    public static void styleTitle(JLabel lbl) {
        lbl.setForeground(HUD_TEXT);
        lbl.setFont(TITLE_FONT);
    }

    public static Border pad(int t, int l, int b, int r) {
        return BorderFactory.createEmptyBorder(t, l, b, r);
    }

    public static JPanel translucentPanel(LayoutManager layout, Color bg) {
        // Paint translucent background via custom paint
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    public static String formatTimeMMSS(long l) {
        int mm = (int) (l / 60);
        int ss = (int) (l % 60);
        return String.format("%02d:%02d", mm, ss);
    }
}
