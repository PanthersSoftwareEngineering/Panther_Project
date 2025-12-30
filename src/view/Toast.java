package view;

import javax.swing.*;
import java.awt.*;

public class Toast {

    private static JWindow window;
    private static Timer hideTimer;

    public static void show(JFrame owner, String message) {
        // ✅ replace previous toast immediately
        if (hideTimer != null) {
            hideTimer.stop();
            hideTimer = null;
        }
        if (window != null) {
            window.setVisible(false);
            window.dispose();
            window = null;
        }

        window = new JWindow(owner);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        panel.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(message);
        lbl.setForeground(UIStyles.GOLD_TEXT);
        lbl.setFont(UIStyles.HUD_FONT);
        panel.add(lbl, BorderLayout.CENTER);

        window.setBackground(new Color(0, 0, 0, 0));
        window.setContentPane(panel);
        window.pack();

        Point p = owner.getLocationOnScreen();
        int x = p.x + (owner.getWidth() - window.getWidth()) / 2;
        int y = p.y + owner.getHeight() - window.getHeight() - 60;
        window.setLocation(x, y);

        window.setAlwaysOnTop(true);
        window.setVisible(true);

        hideTimer = new Timer(2200, e -> {
            if (window != null) {
                window.setVisible(false);
                window.dispose();
                window = null;
            }
            hideTimer = null;
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }
}
