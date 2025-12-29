package view;

import javax.swing.*;
import java.awt.*;

public class Toast extends JWindow {

    // ✅ שליטה מרכזית על גודל/זמן
    private static final int DEFAULT_DURATION_MS = 6000; // היה 2000
    private static final int FONT_SIZE = 26;             // היה 18
    private static final int ARC = 26;                   // פינות עגולות
    private static final int MIN_WIDTH = 520;            // שלא יצא קטן מדי

    public Toast(JFrame owner, String message, int durationMs) {
        super(owner);

        JLabel lbl = new JLabel(message, SwingConstants.CENTER);
        lbl.setForeground(UIStyles.GOLD_TEXT);
        lbl.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));

        // יותר "גדול" ונעים
        lbl.setBorder(UIStyles.pad(18, 34, 18, 34)); // היה 12,20

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // חשוב
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // רקע כהה עם שקיפות
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

                // מסגרת זהובה עדינה
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(new Color(255, 190, 60, 220));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);

                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.add(lbl, BorderLayout.CENTER);

        setContentPane(panel);
        pack();

        // ✅ אוכפים רוחב מינימלי כדי שיראה "גדול"
        if (getWidth() < MIN_WIDTH) {
            setSize(MIN_WIDTH, getHeight());
        }

        // מיקום – מרכז המסך מעל המשחק (אפשר להוריד/להעלות עם /5)
        Point p = owner.getLocationOnScreen();
        int x = p.x + (owner.getWidth() - getWidth()) / 2;
        int y = p.y + owner.getHeight() / 5;
        setLocation(x, y);

        setAlwaysOnTop(true);
        setVisible(true);

        // ✅ סגירה אוטומטית (חד-פעמי)
        Timer t = new Timer(durationMs, e -> dispose());
        t.setRepeats(false);
        t.start();
    }

    public static void show(JFrame owner, String msg) {
        new Toast(owner, msg, DEFAULT_DURATION_MS);
    }

    // אופציונלי: אם בא לך לשלוט בכל קריאה
    public static void show(JFrame owner, String msg, int durationMs) {
        new Toast(owner, msg, durationMs);
    }
}
