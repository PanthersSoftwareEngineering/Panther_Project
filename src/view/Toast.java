package view;

import javax.swing.*;
import java.awt.*;

/**
 * Toast is a lightweight utility class for displaying short, temporary
 * notification messages on top of the game window
 * The toast appears near the bottom of the given frame, stays visible
 * for a short time, and then disappears automatically
 * Only one toast can be displayed at a time
 */
public class Toast {

    /** The currently displayed toast window (if any) */
    private static JWindow window;

    /** Timer responsible for hiding the toast after a fixed delay */
    private static Timer hideTimer;

    /**
     * Displays a toast message centered horizontally above the bottom
     * of the given owner frame
     * If another toast is already visible, it is immediately removed
     * and replaced by the new one
     */
    public static void show(JFrame owner, String message) {

        // Stop and remove any previously scheduled hide operation
        if (hideTimer != null) {
            hideTimer.stop();
            hideTimer = null;
        }

        // Dispose the previous toast window if it still exists
        if (window != null) {
            window.setVisible(false);
            window.dispose();
            window = null;
        }

        // Create a borderless window attached to the owner frame
        window = new JWindow(owner);

        // Custom panel responsible for drawing a rounded, semi-transparent background
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark translucent background
                g2.setColor(new Color(0, 0, 0, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        panel.setLayout(new BorderLayout());

        // Label that displays the toast message
        JLabel lbl = new JLabel(message);
        lbl.setForeground(new Color(230, 235, 255));
        lbl.setFont(UIStyles.HUD_FONT);
        panel.add(lbl, BorderLayout.CENTER);

        // Make the window background fully transparent
        window.setBackground(new Color(0, 0, 0, 0));
        window.setContentPane(panel);
        window.pack();

        // Position the toast centered horizontally, slightly above the bottom of the owner frame
        Point p = owner.getLocationOnScreen();
        int x = p.x + (owner.getWidth() - window.getWidth()) / 2;
        int y = p.y + owner.getHeight() - window.getHeight() - 60;
        window.setLocation(x, y);

        // Ensure the toast is always visible above the game window
        window.setAlwaysOnTop(true);
        window.setVisible(true);

        // Schedule automatic hiding of the toast after ~2.2 seconds
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

