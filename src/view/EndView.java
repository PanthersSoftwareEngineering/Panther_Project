package view;

import controller.AppController;
import model.SysData;

import javax.swing.*;
import java.awt.*;

/**
 * End-of-game screen
 * Optimized layout to ensure all elements stay within the center panel boundaries
 */
public class EndView extends BaseGameFrame {

    public EndView(AppController app, SysData.GameRecord rec) {
        super(app, "Game Over");

        if (app == null) {
            throw new IllegalArgumentException("AppController must not be null");
        }

        Image bgImage = GameAssets.END_BACKGROUND != null
                ? GameAssets.END_BACKGROUND
                : GameAssets.MAIN_BACKGROUND;

        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        boolean won = rec != null && rec.won;

        // TOP SECTION: TITLE
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(Box.createVerticalStrut(30)); 

        JLabel titleLabel = new JLabel(won ? "YOU WON !" : "YOU LOST...");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(UIStyles.ACCENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 80)); // Slightly smaller title

        topPanel.add(titleLabel);
        bgPanel.add(topPanel, BorderLayout.NORTH);

        // CENTER SECTION: STATS CARD
        JPanel centerPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 150)); // Semi-transparent dark background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        // Wrap centerPanel in another panel to maintain its size
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(centerPanel);
        bgPanel.add(wrapperPanel, BorderLayout.CENTER);

        // Data extraction
        String p1     = rec == null ? "-" : safe(rec.p1);
        String p2     = rec == null ? "-" : safe(rec.p2);
        String lvl    = rec == null ? "-" : safe(rec.level);
        String hearts = rec == null ? "-" : String.valueOf(rec.hearts);
        String score  = rec == null ? "-" : String.valueOf(rec.points);

        // Layout constraints
        Insets labelInsets = new Insets(10, 0, 10, 30);
        Insets fieldInsets = new Insets(10, 0, 10, 0);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Player 1
        gc.gridy = 0;
        gc.gridx = 0; gc.insets = labelInsets; gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Player 1 Name"), gc);
        gc.gridx = 1; gc.insets = fieldInsets; gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createWhiteValueField(p1), gc);

        // Row 1: Player 2
        gc.gridy = 1;
        gc.gridx = 0; gc.insets = labelInsets; gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Player 2 Name"), gc);
        gc.gridx = 1; gc.insets = fieldInsets; gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createWhiteValueField(p2), gc);

        // Row 2: Difficulty
        gc.gridy = 2;
        gc.gridx = 0; gc.insets = labelInsets; gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Difficulty Level"), gc);
        gc.gridx = 1; gc.insets = fieldInsets; gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createDarkPillValue(lvl), gc);

        // Row 3: Hearts
        gc.gridy = 3;
        gc.gridx = 0; gc.insets = labelInsets; gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Hearts Left"), gc);
        gc.gridx = 1; gc.insets = fieldInsets; gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createDarkPillValue(hearts), gc);

        // Row 4: Score
        gc.gridy = 4;
        gc.gridx = 0; gc.insets = labelInsets; gc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createFieldTitle("Score"), gc);
        gc.gridx = 1; gc.insets = fieldInsets; gc.anchor = GridBagConstraints.WEST;
        centerPanel.add(createDarkPillValue(score), gc);

        // GIF SECTION
        JLabel gifLabel = new JLabel();
        ImageIcon gifIcon = pickRandomGif(won ? GameAssets.WIN_GIFS : GameAssets.LOSE_GIFS);
        if (gifIcon != null) {
            gifLabel.setIcon(gifIcon);
        }

        GridBagConstraints gcGif = new GridBagConstraints();
        gcGif.gridx = 2; gcGif.gridy = 0; gcGif.gridheight = 5;
        gcGif.insets = new Insets(0, 40, 0, 0);
        gcGif.anchor = GridBagConstraints.CENTER;
        centerPanel.add(gifLabel, gcGif);

        // BOTTOM SECTION: BUTTON
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        ButtonStyled mainMenuBtn = new ButtonStyled("Main Menu");
        mainMenuBtn.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });
        bottom.add(mainMenuBtn);
        bgPanel.add(bottom, BorderLayout.SOUTH);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private JLabel createFieldTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Reduced font size
        return lbl;
    }

    private JComponent createWhiteValueField(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        lbl.setOpaque(true);
        lbl.setBackground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        lbl.setPreferredSize(new Dimension(350, 45)); // Reduced size
        return lbl;
    }

    private JComponent createDarkPillValue(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(15, 17, 26));
        lbl.setBorder(BorderFactory.createLineBorder(UIStyles.ACCENT));
        lbl.setPreferredSize(new Dimension(250, 45)); // Reduced size
        return lbl;
    }

    private String safe(Object o) { return (o == null) ? "-" : o.toString(); }

    private ImageIcon pickRandomGif(ImageIcon[] gifs) {
        if (gifs == null || gifs.length == 0) return null;
        int idx = (int) (Math.random() * gifs.length);
        return gifs[idx];
    }

    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        BackgroundPanel(Image bg) { this.bg = bg; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                double scale = Math.max((double) getWidth() / bg.getWidth(null), (double) getHeight() / bg.getHeight(null));
                int dW = (int) (bg.getWidth(null) * scale);
                int dH = (int) (bg.getHeight(null) * scale);
                g.drawImage(bg, (getWidth() - dW) / 2, (getHeight() - dH) / 2, dW, dH, this);
            }
        }
    }

    private static class ButtonStyled extends JButton {
        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color borderClr = new Color(255, 190, 60);
        ButtonStyled(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 26));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(250, 70));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(baseFill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(borderClr);
            g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 40, 40);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}