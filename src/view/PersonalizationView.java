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

    private final JLabel titleLabel = new JLabel("Personalization");
    private final JLabel previewLabel = new JLabel("Preview: Minesweeper", SwingConstants.CENTER);

    public PersonalizationView(AppController app) {
        super(app, "Personalization");
        this.sys = SysData.getInstance();


        Image bgImage = GameAssets.PERSON_BACKGROUND != null
                ? GameAssets.PERSON_BACKGROUND
                : GameAssets.MAIN_BACKGROUND;

        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);


        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(Box.createVerticalStrut(35));

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(UIStyles.ACCENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 80)); 
        topPanel.add(titleLabel);

        topPanel.add(Box.createVerticalStrut(10));

        JLabel sub = new JLabel("Choose your accent color. It will remain until you change it again.");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setForeground(new Color(255, 255, 255, 220));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        topPanel.add(sub);

        bgPanel.add(topPanel, BorderLayout.NORTH);


        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 150)); // like EndView
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);


        // Wrapper keeps card size centered (like EndView)
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        bgPanel.add(wrapper, BorderLayout.CENTER);

        // ===== Content inside card =====
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.weightx = 1;

        JLabel chooseLbl = new JLabel("Choose Accent Color", SwingConstants.CENTER);
        chooseLbl.setForeground(Color.WHITE);
        chooseLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 20, 0);
        card.add(chooseLbl, gc);

        JPanel grid = new JPanel(new GridLayout(0, 2, 30, 16));
        grid.setOpaque(false);

        Color current = sys.getAccentColor();

        for (Map.Entry<String, Color> entry : ACCENTS.entrySet()) {
            String name = entry.getKey();
            Color color = entry.getValue();

            JRadioButton rb = createRadio(name, color);
            if (colorsClose(color, current)) rb.setSelected(true);

            rb.addActionListener(e -> updatePreview(color));

            accentGroup.add(rb);
            grid.add(rb);
        }

        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 28, 0);
        card.add(grid, gc);

        JLabel prevTitle = new JLabel("Live Preview", SwingConstants.CENTER);
        prevTitle.setForeground(Color.WHITE);
        prevTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 10, 0);
        card.add(prevTitle, gc);

        previewLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        previewLabel.setForeground(current);
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 0, 0);
        card.add(previewLabel, gc);

        // ===== BOTTOM: Buttons (Save + Back only) =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 45, 0));

        RoundedButton saveBtn = new RoundedButton("Save", 240, 75, 26);
        RoundedButton backBtn = new RoundedButton("Back", 240, 75, 26);

        saveBtn.addActionListener(e -> {
            saveTheme();
            showToast("Theme saved!", 1800);
        });

        backBtn.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });

        bottom.add(saveBtn);
        bottom.add(backBtn);

        bgPanel.add(bottom, BorderLayout.SOUTH);

        // toast overlay
        installToastLayer();

        // initial preview
        updatePreview(current);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private JRadioButton createRadio(String name, Color color) {
        JRadioButton rb = new JRadioButton(name);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setForeground(new Color(255, 255, 255, 230));
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        rb.setIcon(colorDotIcon(new Color(255, 255, 255, 70), 18));
        rb.setSelectedIcon(colorDotIcon(color, 18));

        // More clickable area
        rb.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return rb;
    }

    private void saveTheme() {
        Color selected = getSelectedAccent();

        // persist
        sys.setAccentColor(selected);

        // apply immediately
        UIStyles.setAccent(selected);

        // update title color now too
        titleLabel.setForeground(UIStyles.ACCENT);

        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    private void updatePreview(Color accent) {
        previewLabel.setForeground(accent);
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

    private boolean colorsClose(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed()) < 3
                && Math.abs(a.getGreen() - b.getGreen()) < 3
                && Math.abs(a.getBlue() - b.getBlue()) < 3;
    }

    private Icon colorDotIcon(Color c, int size) {
        return new Icon() {
            @Override public void paintIcon(Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int d = size;
                int px = x + 6;
                int py = y + 6;

                g2.setColor(c);
                g2.fillOval(px, py, d, d);

                g2.setColor(new Color(0, 0, 0, 140));
                g2.drawOval(px, py, d, d);

                g2.dispose();
            }
            @Override public int getIconWidth()  { return size + 18; }
            @Override public int getIconHeight() { return size + 18; }
        };
    }
}
