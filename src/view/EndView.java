package view;

import controller.AppController;
import model.SysData;

import javax.swing.*;
import java.awt.*;

/**
 * End-of-game screen (Win/Lose).
 * Cleaned + consistent sizing for all value fields.
 */
public class EndView extends BaseGameFrame {

    // ---------- tuning constants ----------
    private static final Color GOLD      = new Color(255, 190, 60);
    private static final Color DARK_FILL = new Color(15, 17, 26);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 90);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 34);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.PLAIN, 30);

    private static final Dimension WHITE_VALUE_SIZE = new Dimension(520, 60);
    private static final Dimension DARK_VALUE_SIZE  = new Dimension(350, 60);

    public EndView(AppController app, SysData.GameRecord rec) {
        super(app, "Game Over");
        if (app == null) throw new IllegalArgumentException("AppController must not be null");

        boolean won = rec != null && rec.won;

        // ===== background =====
        Image bgImage = (GameAssets.END_BACKGROUND != null)
                ? GameAssets.END_BACKGROUND
                : GameAssets.MAIN_BACKGROUND;

        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // ===== TOP (title) =====
        bgPanel.add(buildTop(won), BorderLayout.NORTH);

        // ===== CENTER (stats + gif) =====
        bgPanel.add(buildCenter(rec, won), BorderLayout.CENTER);

        // ===== BOTTOM (button) =====
        bgPanel.add(buildBottom(app), BorderLayout.SOUTH);

        // ===== frame basics =====
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    // =====================================================================================
    // PANELS
    // =====================================================================================

    private JComponent buildTop(boolean won) {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(Box.createVerticalStrut(40));

        JLabel title = new JLabel(won ? "YOU WON !" : "YOU LOST...");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(255, 204, 0));
        title.setFont(TITLE_FONT);

        top.add(title);
        top.add(Box.createVerticalStrut(10));
        return top;
    }

    private JComponent buildCenter(SysData.GameRecord rec, boolean won) {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(40, 80, 150, 80));

        // safe values
        String p1     = safe(rec == null ? null : rec.p1);
        String p2     = safe(rec == null ? null : rec.p2);
        String lvl    = safe(rec == null ? null : rec.level);
        String hearts = (rec == null) ? "-" : String.valueOf(rec.hearts);
        String score  = (rec == null) ? "-" : String.valueOf(rec.points);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;

        Insets leftInsets  = new Insets(22, 0, 22, 40);
        Insets rightInsets = new Insets(22, 10, 22, 0);

        // Row 0
        addRow(center, gc, 0, "Player 1 Name", createWhiteValue(p1), leftInsets, rightInsets);
        // Row 1
        addRow(center, gc, 1, "Player 2 Name", createWhiteValue(p2), leftInsets, rightInsets);
        // Row 2
        addRow(center, gc, 2, "Difficulty Level", createDarkValue(lvl), leftInsets, rightInsets);
        // Row 3
        addRow(center, gc, 3, "Hearts Left", createDarkValue(hearts), leftInsets, rightInsets);
        // Row 4
        addRow(center, gc, 4, "Score", createDarkValue(score), leftInsets, rightInsets);

        // GIF
        JLabel gifLabel = new JLabel();
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gifLabel.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon gifIcon = pickRandomGif(won ? GameAssets.WIN_GIFS : GameAssets.LOSE_GIFS);
        if (gifIcon != null) gifLabel.setIcon(gifIcon);

        GridBagConstraints gcGif = new GridBagConstraints();
        gcGif.gridx = 2;
        gcGif.gridy = 0;
        gcGif.gridheight = 5;
        gcGif.insets = new Insets(10, 60, 275, 0);
        gcGif.anchor = GridBagConstraints.CENTER;
        center.add(gifLabel, gcGif);

        return center;
    }

    private JComponent buildBottom(AppController app) {
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 80, 170));

        bottom.add(Box.createHorizontalGlue());

        ButtonStyled mainMenuBtn = new ButtonStyled("Main Menu");
        bottom.add(mainMenuBtn);

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

        return bottom;
    }

    // =====================================================================================
    // ROW HELPERS
    // =====================================================================================

    private void addRow(JPanel parent,
                        GridBagConstraints gc,
                        int row,
                        String leftText,
                        JComponent rightValue,
                        Insets leftInsets,
                        Insets rightInsets) {

        // left label
        gc.gridy = row;
        gc.gridx = 0;
        gc.insets = leftInsets;
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        parent.add(createLeftLabel(leftText), gc);

        // right value
        gc.gridx = 1;
        gc.insets = rightInsets;
        gc.anchor = GridBagConstraints.WEST;
        parent.add(rightValue, gc);
    }

    private JLabel createLeftLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(LABEL_FONT);
        return lbl;
    }

    /**
     * White rounded field for player names.
     * IMPORTANT: fixed height/width so it matches other rows consistently.
     */
    private JComponent createWhiteValue(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(VALUE_FONT);
        lbl.setOpaque(true);
        lbl.setBackground(Color.WHITE);

        lbl.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(200, 200, 200), 3, 10),
                BorderFactory.createEmptyBorder(10, 40, 10, 40)
        ));

        setFixedSize(lbl, WHITE_VALUE_SIZE);
        return lbl;
    }

    /**
     * Dark pill with gold border for difficulty/hearts/score.
     */
    private JComponent createDarkValue(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(VALUE_FONT);
        lbl.setOpaque(true);
        lbl.setBackground(DARK_FILL);

        lbl.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(GOLD, 4, 10),
                BorderFactory.createEmptyBorder(10, 80, 10, 80)
        ));

        setFixedSize(lbl, DARK_VALUE_SIZE);
        return lbl;
    }

    private void setFixedSize(JComponent c, Dimension d) {
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
    }

    private String safe(Object o) {
        String s = (o == null) ? "-" : o.toString();
        return s.isBlank() ? "-" : s;
    }

    private ImageIcon pickRandomGif(ImageIcon[] gifs) {
        if (gifs == null || gifs.length == 0) return null;
        for (int tries = 0; tries < gifs.length; tries++) {
            int idx = (int) (Math.random() * gifs.length);
            if (gifs[idx] != null) return gifs[idx];
        }
        return null;
    }

    // =====================================================================================
    // BACKGROUND PANEL
    // =====================================================================================

    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        BackgroundPanel(Image bg) { this.bg = bg; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null) {
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

            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            g.drawImage(bg, x, y, drawW, drawH, this);
        }
    }

    // =====================================================================================
    // BUTTON
    // =====================================================================================

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

    // =====================================================================================
    // ROUNDED BORDER
    // =====================================================================================

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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