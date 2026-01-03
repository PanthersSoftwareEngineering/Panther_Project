package view;

import controller.AppController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public abstract class BaseGameFrame extends JFrame {

    protected final AppController app;

    // =========================================================
    //  In-frame popup (toast)
    // =========================================================
    private final JLabel popupLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel popupPanel = new JPanel(new BorderLayout());
    private Timer popupTimer;

    protected BaseGameFrame(AppController app, String title) {
        super(title);
        this.app = app;

        // --- Changing the Java Coffee Icon to a bomb one ---
        try {
            // Load the image
            // Set the taskbar and window icon
            if (GameAssets.GAME_ICON != null) {
                this.setIconImage(GameAssets.GAME_ICON);
            }
        } catch (Exception e) {
            System.err.println("Could not load game icon: " + e.getMessage());
            System.out.println("[DEBUG] Game icon is null. Check path in GameAssets.");
        }

        // --- close behaviour: we decide in confirmExit() ---
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // --- full screen with OS bar ---
        setResizable(false);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);

        // when user clicks the OS X/close button
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });

        // prepare reusable toast UI
        initToastUI();
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
     * Exit Dialog
     * return true if user clicked "Yes"
     */
    private boolean showStyledExitDialog() {
        JDialog dialog = new JDialog(this, "Confirm Exit", true);
        dialog.setUndecorated(true);
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel();
        root.setBackground(new Color(12, 12, 20, 240));
        // ✅ use dynamic accent
        root.setBorder(BorderFactory.createLineBorder(UIStyles.ACCENT, 3));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        dialog.setContentPane(root);

        root.add(Box.createVerticalStrut(25));

        JLabel msg = new JLabel("Are you sure you want to exit the game?");
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        // ✅ use dynamic accent
        msg.setForeground(UIStyles.ACCENT);
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
    //  Reusable in-frame toast (popup message)
    // =========================================================

    protected final void installToastLayer() {
        // Wrap current content so we can overlay the popup above it
        Container current = getContentPane();

        // avoid double-install
        if (current instanceof JLayeredPane) return;

        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(null);

        // content container
        JPanel contentHolder = new JPanel(new BorderLayout());
        contentHolder.setOpaque(false);
        contentHolder.add(current, BorderLayout.CENTER);

        // size everything on resize
        layered.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layered.getWidth();
                int h = layered.getHeight();

                contentHolder.setBounds(0, 0, w, h);

                // popup centered near top
                int pw = Math.min(760, (int) (w * 0.70));
                int ph = 70;
                int px = (w - pw) / 2;
                int py = Math.max(30, h / 8);

                popupPanel.setBounds(px, py, pw, ph);
                popupPanel.revalidate();
                popupPanel.repaint();
            }
        });

        layered.add(contentHolder, Integer.valueOf(0));
        layered.add(popupPanel, Integer.valueOf(200));

        setContentPane(layered);

        // force initial layout sizing
        SwingUtilities.invokeLater(() -> {
            layered.setSize(getSize());
            layered.dispatchEvent(new ComponentEvent(layered, ComponentEvent.COMPONENT_RESIZED));
        });
    }

    protected final void showToast(String message) {
        showToast(message, 2000);
    }

    /** Show a popup message inside the same frame (non-blocking) */
    protected final void showToast(String message, int durationMs) {
        popupLabel.setText(message);
        popupPanel.setVisible(true);

        if (popupTimer != null) popupTimer.stop();
        popupTimer = new Timer(durationMs, e -> popupPanel.setVisible(false));
        popupTimer.setRepeats(false);
        popupTimer.start();
    }

    private void initToastUI() {
        // ✅ dynamic accent
        popupLabel.setForeground(UIStyles.ACCENT);
        popupLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        popupLabel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        popupPanel.setOpaque(false);
        popupPanel.setVisible(false);

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // dark translucent bubble
                g2.setColor(new Color(0, 0, 0, 185));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                // ✅ accent border (dynamic)
                Color a = UIStyles.ACCENT;
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 200));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 22, 22);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.add(popupLabel, BorderLayout.CENTER);

        popupPanel.removeAll();
        popupPanel.add(bubble, BorderLayout.CENTER);
    }

    // =========================================================
    //  Reusable rounded button style
    // =========================================================
    public static class RoundedButton extends JButton {

        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final int radius = 65;

        public RoundedButton(String text, int width, int height, int fontSize) {
            super(text);

            setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            // ✅ initial; paintComponent keeps it synced anyway
            setForeground(UIStyles.ACCENT);

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

            // ✅ dynamic accent
            Color accent = UIStyles.ACCENT;

            g2.setStroke(new BasicStroke(4f));
            g2.setColor(accent);
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            // keep text synced too
            if (!accent.equals(getForeground())) {
                setForeground(accent);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
