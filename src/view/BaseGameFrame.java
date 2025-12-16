package view;

import controller.AppController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Base frame for all game windows.
 * - Full screen
 * - Not resizable, not draggable
 * - Custom exit confirmation
 */
public abstract class BaseGameFrame extends JFrame {

    protected final AppController app;
    private Point fixedLocation;   // to prevent dragging

    protected BaseGameFrame(AppController app, String title) {
        super(title);
        this.app = app;

        // --- close behaviour: we decide in confirmExit() ---
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // --- full screen with OS bar ---
        setResizable(false);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);
        fixedLocation = getLocation();

        // prevent dragging (force window back to fixedLocation)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                if (!getLocation().equals(fixedLocation)) {
                    SwingUtilities.invokeLater(() -> setLocation(fixedLocation));
                }
            }
        });

        // when user clicks the OS X/close button
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });
    }

    /** Show the window */
    public void showSelf() {
        if (!isVisible()) {
            setVisible(true);
        }
    }

    // =========================================================
    //  Exit confirmation – default behaviour: ask & exit game
    // =========================================================
    protected void confirmExit() {
        boolean shouldExit = showStyledExitDialog();
        if (shouldExit) {
            System.exit(0);
        }
    }

    /**
     *Exit Dialog
     * @return true if user clicked "Yes".
     */
    private boolean showStyledExitDialog() {
        JDialog dialog = new JDialog(this, "Confirm Exit", true);
        dialog.setUndecorated(true);             // we draw our own border
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel();
        root.setBackground(new Color(12, 12, 20, 240));
        root.setBorder(BorderFactory.createLineBorder(new Color(255, 190, 60), 3));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        dialog.setContentPane(root);

        root.add(Box.createVerticalStrut(25));

        JLabel msg = new JLabel("Are you sure you want to exit the game?");
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        msg.setForeground(Color.WHITE);
        msg.setFont(new Font("Segoe UI", Font.BOLD, 20));
        root.add(msg);

        root.add(Box.createVerticalStrut(30));

        JPanel buttonsRow = new JPanel();
        buttonsRow.setOpaque(false);
        buttonsRow.setLayout(new BoxLayout(buttonsRow, BoxLayout.X_AXIS));

        RoundedButton yesBtn = new RoundedButton("Yes", 160, 60, 22);
        RoundedButton noBtn  = new RoundedButton("No", 160, 60, 22);

        buttonsRow.add(Box.createHorizontalGlue());
        buttonsRow.add(yesBtn);
        buttonsRow.add(Box.createHorizontalStrut(20));
        buttonsRow.add(noBtn);
        buttonsRow.add(Box.createHorizontalGlue());

        root.add(buttonsRow);
        root.add(Box.createVerticalStrut(25));

        final boolean[] result = {false};

        yesBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        noBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        dialog.getRootPane().setDefaultButton(noBtn); // Enter = No (safer)
        dialog.setVisible(true);

        return result[0];
    }

    // =========================================================
    //  Reusable rounded button style
    // =========================================================
    protected static class RoundedButton extends JButton {

        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final Color borderClr = new Color(255, 190, 60);
        private final int radius = 65;

        public RoundedButton(String text, int width, int height, int fontSize) {
            super(text);

            setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            setForeground(Color.WHITE);

            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);

            setPreferredSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            int w = getWidth();
            int h = getHeight();

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(4f));
            g2.setColor(borderClr);
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
