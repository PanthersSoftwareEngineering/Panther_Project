package view;

import controller.AppController;
import model.SysData;

import javax.swing.*;
import java.awt.*;

/**
 * End-of-game screen.
 */
public class EndView extends BaseGameFrame {

    public EndView(AppController app, SysData.GameRecord rec) {
        super(app, "Game Over");

        if (app == null) {
            throw new IllegalArgumentException("AppController must not be null");
        }

        // ===== choose background image (end screen first, then main as fallback) =====
        Image bgImage = GameAssets.END_BACKGROUND != null
                ? GameAssets.END_BACKGROUND
                : GameAssets.MAIN_BACKGROUND;

        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        boolean won = rec != null && rec.won;

        // =================================================================================
        // TOP: TITLE
        // =================================================================================
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(Box.createVerticalStrut(40));   // distance from top edge

        JLabel titleLabel = new JLabel(won ? "YOU WON !" : "YOU LOST...");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(255, 204, 0));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 90));

        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(10));

        bgPanel.add(topPanel, BorderLayout.NORTH);

        // =================================================================================
        // CENTER: STATS + GIF
        // =================================================================================
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 150, 80));
        bgPanel.add(centerPanel, BorderLayout.CENTER);

        // values from GameRecord (safe defaults)
        String p1     = rec == null ? "-" : safe(rec.p1);
        String p2     = rec == null ? "-" : safe(rec.p2);
        String lvl    = rec == null ? "-" : safe(rec.level);
        String hearts = rec == null ? "-" : String.valueOf(rec.hearts);
        String score  = rec == null ? "-" : String.valueOf(rec.points);

        // ---- shared row spacing ----
        Insets leftColInsets  = new Insets(22, 0, 22, 40);
        Insets rightColInsets = new Insets(22, 10, 22, 0);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;

        // ---------------- Row 0: Player 1 ----------------
        // left column: label
        gc.gridx = 0;
        gc.insets = leftColInsets;
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        centerPanel.add(createFieldTitle("Player 1 Name"), gc);

        // right column: value
        gc.gridx = 1;
        gc.insets = rightColInsets;
        gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createWhiteValueField(p1), gc);

        // ---------------- Row 1: Player 2 ----------------
        gc.gridy = 1;

        gc.gridx = 0;
        gc.insets = leftColInsets;
        gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Player 2 Name"), gc);

        gc.gridx = 1;
        gc.insets = rightColInsets;
        gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createWhiteValueField(p2), gc);

        // ---------------- Row 2: Difficulty ----------------
        gc.gridy = 2;

        gc.gridx = 0;
        gc.insets = leftColInsets;
        gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Difficulty Level"), gc);

        gc.gridx = 1;
        gc.insets = rightColInsets;
        gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createDarkPillValue(lvl), gc);

        // ---------------- Row 3: Hearts left ----------------
        gc.gridy = 3;

        gc.gridx = 0;
        gc.insets = leftColInsets;
        gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Hearts Left"), gc);

        gc.gridx = 1;
        gc.insets = rightColInsets;
        gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createDarkPillValue(hearts), gc);

        // ---------------- Row 4: Score ----------------
        gc.gridy = 4;

        gc.gridx = 0;
        gc.insets = leftColInsets;
        gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Score"), gc);

        gc.gridx = 1;
        gc.insets = rightColInsets;
        gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createDarkPillValue(score), gc);

        // ---------------- GIF  ----------------
        JLabel gifLabel = new JLabel();
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gifLabel.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon gifIcon = pickRandomGif(won ? GameAssets.WIN_GIFS : GameAssets.LOSE_GIFS);
        if (gifIcon != null) {
            gifLabel.setIcon(gifIcon);
        }

        GridBagConstraints gcGif = new GridBagConstraints();
        gcGif.gridx = 2;
        gcGif.gridy = 0;
        gcGif.gridheight = 5;
        gcGif.insets = new Insets(10, 60, 275, 0);
        gcGif.anchor = GridBagConstraints.CENTER;
        gcGif.fill = GridBagConstraints.NONE;
        gcGif.weightx = 0;
        centerPanel.add(gifLabel, gcGif);

        // =================================================================================
        // BOTTOM: MAIN MENU BUTTON
        // =================================================================================
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 80, 170));

        bottom.add(Box.createHorizontalGlue());
        ButtonStyled mainMenuBtn = new ButtonStyled("Main Menu");
        bottom.add(mainMenuBtn);

        bgPanel.add(bottom, BorderLayout.SOUTH);

        mainMenuBtn.addActionListener(e -> {
            try {
                app.showMainMenu();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "An error occurred while returning to the main menu:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } finally {
                dispose();
            }
        });

        // =================================================================================
        // FRAME BASICS
        // =================================================================================
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // start maximized like other views
        setVisible(true);
    }

    // =====================================================================================
    // UI HELPERS
    // =====================================================================================

    /** Title for each field on the left ("Player 1 Name", "Difficulty Level", etc.). */
    private JLabel createFieldTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 34));
        return lbl;
    }

    /** White rounded field used for player names. */
    private JComponent createWhiteValueField(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        lbl.setOpaque(true);
        lbl.setBackground(Color.WHITE);

        // Small rounded border (radius 10)
        lbl.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(200, 200, 200), 3, 10),
                BorderFactory.createEmptyBorder(10, 40, 10, 40)
        ));

        Dimension d = new Dimension(520, 60);
        lbl.setPreferredSize(d);
        lbl.setMinimumSize(d);
        lbl.setMaximumSize(d);
        return lbl;
    }

    /** Dark pill with golden border used for difficulty / hearts / score. */
    private JComponent createDarkPillValue(String text) {
        Color gold = new Color(255, 190, 60);

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(15, 17, 26));

        lbl.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(gold, 4, 10),
                BorderFactory.createEmptyBorder(10, 80, 10, 80)
        ));

        Dimension d = new Dimension(350, 60);
        lbl.setPreferredSize(d);
        lbl.setMinimumSize(d);
        lbl.setMaximumSize(d);
        return lbl;
    }

    /** Returns "-" instead of null when showing text values. */
    private String safe(Object o) {
        return (o == null) ? "-" : o.toString();
    }

    /** Picks a random non-null GIF icon from the preloaded arrays in GameAssets. */
    private ImageIcon pickRandomGif(ImageIcon[] gifs) {
        if (gifs == null || gifs.length == 0) return null;
        for (int tries = 0; tries < gifs.length; tries++) {
            int idx = (int) (Math.random() * gifs.length);
            if (gifs[idx] != null) return gifs[idx];
        }
        return null;
    }

    // =====================================================================================
    // BACKGROUND PANEL – draws scaled background image 
    // =====================================================================================

    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        BackgroundPanel(Image bg) { this.bg = bg; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null) {
                // Fallback gradient if background image is missing
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(12, 12, 20),
                        0, getHeight(), new Color(40, 40, 70)
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
                return;
            }

            int imgW = bg.getWidth(null);
            int imgH = bg.getHeight(null);
            int panelW = getWidth();
            int panelH = getHeight();
            if (imgW <= 0 || imgH <= 0) return;

            // "Cover" behavior – scale image to fill entire panel
            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            g.drawImage(bg, x, y, drawW, drawH, this);
        }
    }


    private static class ButtonStyled extends JButton {
        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final Color borderClr = new Color(255, 190, 60);
        private final int radius = 45;

        ButtonStyled(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 32));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            Dimension d = new Dimension(320, 90);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);

            setHorizontalAlignment(SwingConstants.CENTER);
            setRolloverEnabled(true);
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


    private static class RoundedBorder implements javax.swing.border.Border {
        private final Color color;
        private final int thickness;
        private final int radius;

        RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
