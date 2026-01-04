package view;

import controller.AppController;
import model.SysData;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PersonalizationView extends BaseGameFrame {

    private final SysData sys;

    private static final Color DEFAULT_GOLD = new Color(255, 204, 0);

    private static final Map<String, Color> ACCENTS = new LinkedHashMap<>();
    static {
        ACCENTS.put("Golden (Default)", DEFAULT_GOLD);
        ACCENTS.put("Royal Blue",       new Color(0x3B82F6));
        ACCENTS.put("Emerald Green",    new Color(0x10B981));
        ACCENTS.put("Purple",           new Color(0x8B5CF6));
        ACCENTS.put("Crimson",          new Color(0xEF4444));
        ACCENTS.put("Cyan / Aqua",      new Color(0x22D3EE));
    }

    private final ButtonGroup accentGroup = new ButtonGroup();

    // Screen header
    private final JLabel titleLabel = new JLabel("Personalization");

    // Preview label under the preview card
    private final JLabel previewLabel = new JLabel("Preview: Minesweeper", SwingConstants.LEFT);

    // Pending (not yet saved) accent
    private Color pendingAccent;

    // These must be fields so updatePreview() can update them
    private JLabel miniTitle;
    private JPanel previewCard;

    public PersonalizationView(AppController app) {
        super(app, "Personalization");
        this.sys = SysData.getInstance();

        Image bgImage = GameAssets.PERSON_BACKGROUND != null
                ? GameAssets.PERSON_BACKGROUND
                : GameAssets.MAIN_BACKGROUND;

        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // ---------- TOP: title + subtitle ----------
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(Box.createVerticalStrut(40));

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(UIStyles.ACCENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        topPanel.add(titleLabel);

        topPanel.add(Box.createVerticalStrut(12));

        JLabel sub = new JLabel("Choose your accent color. It will remain until you change it again.");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setForeground(new Color(255, 255, 255, 220));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        topPanel.add(sub);

        topPanel.add(Box.createVerticalStrut(10));
        bgPanel.add(topPanel, BorderLayout.NORTH);

        // ---------- initial accent ----------
        Color current = sys.getAccentColor();
        pendingAccent = current;

        // ---------- CENTER: wide card with 2 columns ----------
        JPanel mainCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
                g2.dispose();
            }
        };
        mainCard.setOpaque(false);
        mainCard.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));
        mainCard.setPreferredSize(new Dimension(1200, 520));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(mainCard);
        bgPanel.add(wrapper, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        // ---------- Two columns: left (choices), right (preview) ----------
        JPanel columns = new JPanel(new GridLayout(1, 2, 50, 0));
        columns.setOpaque(false);

        // LEFT: color selection
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel chooseLbl = new JLabel("Choose Accent Color", SwingConstants.LEFT);
        chooseLbl.setForeground(Color.WHITE);
        chooseLbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        chooseLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(chooseLbl);
        leftCol.add(Box.createVerticalStrut(18));

        JPanel tilesGrid = new JPanel(new GridLayout(0, 2, 18, 18));
        tilesGrid.setOpaque(false);
        tilesGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Map.Entry<String, Color> entry : ACCENTS.entrySet()) {
            String name = entry.getKey();
            Color color = entry.getValue();

            ColorTileRadio rb = new ColorTileRadio(name, color);
            if (colorsClose(color, current)) rb.setSelected(true);

            rb.addActionListener(e -> updatePreview(color));

            accentGroup.add(rb);
            tilesGrid.add(rb);
        }

        leftCol.add(tilesGrid);

        // RIGHT: live preview
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        JLabel prevTitle = new JLabel("Live Preview", SwingConstants.LEFT);
        prevTitle.setForeground(Color.WHITE);
        prevTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        prevTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightCol.add(prevTitle);
        rightCol.add(Box.createVerticalStrut(18));

        // Preview card (field) so repaint uses latest pendingAccent
        previewCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 165));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                g2.setStroke(new BasicStroke(3f));
                g2.setColor(pendingAccent);
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 22, 22);

                g2.dispose();
            }
        };
        previewCard.setOpaque(false);
        previewCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        previewCard.setPreferredSize(new Dimension(520, 240));
        previewCard.setMaximumSize(new Dimension(520, 240));
        previewCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints pc = new GridBagConstraints();
        pc.gridx = 0;
        pc.fill = GridBagConstraints.HORIZONTAL;

        // Preview title (field) so updatePreview() can change its color
        miniTitle = new JLabel("MineSweeper", SwingConstants.CENTER);
        miniTitle.setForeground(pendingAccent);
        miniTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));

        JLabel miniSub = new JLabel("This is how buttons & titles will look", SwingConstants.CENTER);
        miniSub.setForeground(new Color(255, 255, 255, 200));
        miniSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        PreviewButton sampleBtn = new PreviewButton("Sample Button");

        pc.gridy = 0;
        pc.insets = new Insets(0, 0, 10, 0);
        previewCard.add(miniTitle, pc);

        pc.gridy = 1;
        pc.insets = new Insets(0, 0, 18, 0);
        previewCard.add(miniSub, pc);

        pc.gridy = 2;
        pc.insets = new Insets(0, 0, 0, 0);
        previewCard.add(sampleBtn, pc);

        rightCol.add(previewCard);
        rightCol.add(Box.createVerticalStrut(12));

       

        columns.add(leftCol);
        columns.add(rightCol);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 0);
        mainCard.add(columns, gc);

        // ---------- BOTTOM: Back / Save buttons (raised) ----------
        JPanel bottomButtons = new JPanel();
        bottomButtons.setOpaque(false);
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.X_AXIS));
        bottomButtons.setMaximumSize(new Dimension(1200, 120));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(10, 90, 0, 90));

        ButtonStyled backBtn = new ButtonStyled("Back");
        ButtonStyled saveBtn = new ButtonStyled("Save");

        bottomButtons.add(Box.createHorizontalGlue());
        bottomButtons.add(backBtn);
        bottomButtons.add(Box.createHorizontalStrut(280));
        bottomButtons.add(saveBtn);
        bottomButtons.add(Box.createHorizontalGlue());

        // Wrap in SOUTH so we can add space UNDER them (moves them up)
        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.setOpaque(false);
        southWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 55, 0));
        southWrap.add(bottomButtons, BorderLayout.NORTH);

        bgPanel.add(southWrap, BorderLayout.SOUTH);

        // Actions
        backBtn.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });

        saveBtn.addActionListener(e -> {
            saveTheme();
            showToast("Theme saved!", 1800);
        });

        installToastLayer();
        updatePreview(current);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void saveTheme() {
        Color selected = getSelectedAccent();
        sys.setAccentColor(selected);
        UIStyles.setAccent(selected);

        titleLabel.setForeground(UIStyles.ACCENT);
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    private void updatePreview(Color accent) {
        pendingAccent = accent;

        previewLabel.setForeground(accent);
        previewLabel.setText("Preview: " + nameForColor(accent));

        if (miniTitle != null) miniTitle.setForeground(accent);
        if (previewCard != null) previewCard.repaint();

        repaint();
    }

    private Color getSelectedAccent() {
        for (Map.Entry<String, Color> entry : ACCENTS.entrySet()) {
            String name = entry.getKey();
            Color c = entry.getValue();

            for (var e = accentGroup.getElements(); e.hasMoreElements();) {
                AbstractButton b = e.nextElement();
                if (b.isSelected() && b.getText().equals(name)) return c;
            }
        }
        return DEFAULT_GOLD;
    }

    private String nameForColor(Color color) {
        for (Map.Entry<String, Color> e : ACCENTS.entrySet()) {
            if (colorsClose(color, e.getValue())) return e.getKey();
        }
        return "Custom";
    }

    private boolean colorsClose(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed()) < 3
                && Math.abs(a.getGreen() - b.getGreen()) < 3
                && Math.abs(a.getBlue() - b.getBlue()) < 3;
    }

    // ---------- Background panel ----------
    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        public BackgroundPanel(Image bg) { this.bg = bg; }

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

    // ---------- Bottom buttons (same style as NewMatch) ----------
    private static class ButtonStyled extends JButton {
        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final int radius = 45;

        public ButtonStyled(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 32));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            setHorizontalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);

            Dimension d = new Dimension(320, 90);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);

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
            g2.setColor(UIStyles.ACCENT);
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Preview button inside card (uses pendingAccent) ----------
    private class PreviewButton extends JButton {
        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final int radius = 40;

        public PreviewButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 20));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            Dimension d = new Dimension(260, 64);
            setPreferredSize(d);

            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(pendingAccent);
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Custom radio tile (dot never overlaps text) ----------
    private static class ColorTileRadio extends JRadioButton {
        private final Color color;
        private final Color baseFill  = new Color(20, 24, 32, 160);
        private final Color hoverFill = new Color(20, 24, 32, 200);
        private final int radius = 18;

        private static final int DOT_D = 18;
        private static final int DOT_X = 18;
        private static final int TEXT_LEFT_PAD = 52;

        public ColorTileRadio(String name, Color color) {
            super(name);
            this.color = color;

            setOpaque(false);
            setFocusPainted(false);
            setFocusable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);

            setForeground(new Color(255, 255, 255, 230));
            setFont(new Font("Segoe UI", Font.PLAIN, 22));

            setIcon(new EmptyIcon(0, 0));
            setSelectedIcon(new EmptyIcon(0, 0));

            setBorder(BorderFactory.createEmptyBorder(16, TEXT_LEFT_PAD, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(isSelected() ? UIStyles.ACCENT : new Color(255, 255, 255, 60));
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();

            super.paintComponent(g);

            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cy = (h - DOT_D) / 2;

            g3.setColor(color);
            g3.fillOval(DOT_X, cy, DOT_D, DOT_D);

            g3.setColor(new Color(0, 0, 0, 140));
            g3.drawOval(DOT_X, cy, DOT_D, DOT_D);

            g3.dispose();
        }
    }

    // ---------- Tiny helper icon for "no radio icon" ----------
    private static class EmptyIcon implements Icon {
        private final int w, h;
        public EmptyIcon(int w, int h) { this.w = w; this.h = h; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override public int getIconWidth() { return w; }
        @Override public int getIconHeight() { return h; }
    }
}
