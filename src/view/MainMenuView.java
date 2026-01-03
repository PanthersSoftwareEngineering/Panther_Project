package view;

import controller.AppController;
import view.GameAssets;

import javax.swing.*;
import java.awt.*;

/**
 * Main menu screen.
 * Uses BaseGameFrame for consistent window behavior and styling.
 * Shows a visible title (not embedded in the background) and the main navigation buttons.
 */
public class MainMenuView extends BaseGameFrame {

    public MainMenuView(AppController app) {
        super(app, "Minesweeper");

        // ===== background =====
        Image bgImage = GameAssets.MAIN_BACKGROUND;
        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new GridBagLayout());
        setContentPane(bgPanel);

        // ===== main vertical panel =====
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Move title + buttons slightly higher
        mainPanel.add(Box.createVerticalStrut(40));

        // ===== TITLE (ALWAYS VISIBLE) =====
        JLabel title = new JLabel("MineSweeper", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(UIStyles.ACCENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 100));

        // Optional: subtle shadow-ish border using a transparent panel behind the title
        // (helps readability on bright backgrounds without adding a "grey card")
        JPanel titleWrap = new JPanel(new GridBagLayout());
        titleWrap.setOpaque(false);
        titleWrap.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        titleWrap.add(title);

        mainPanel.add(titleWrap);

        // spacing between title and buttons (slightly higher than before)
        mainPanel.add(Box.createVerticalStrut(55));

        // ===== buttons column =====
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));

        int gap = 30;

        RoundedButton newGameBtn = new RoundedButton("New Game", 700, 90, 50);
        RoundedButton historyBtn = new RoundedButton("Games History", 700, 90, 50);
        RoundedButton qmanBtn    = new RoundedButton("Questions Management", 700, 90, 50);
        RoundedButton personalizationBtn = new RoundedButton("Personalization", 700, 90, 50);
        RoundedButton exitBtn    = new RoundedButton("Exit", 700, 90, 50);

        buttonsPanel.add(newGameBtn);
        buttonsPanel.add(Box.createVerticalStrut(gap));
        buttonsPanel.add(historyBtn);
        buttonsPanel.add(Box.createVerticalStrut(gap));
        buttonsPanel.add(qmanBtn);
        buttonsPanel.add(Box.createVerticalStrut(gap));
        buttonsPanel.add(personalizationBtn);
        buttonsPanel.add(Box.createVerticalStrut(gap));
        buttonsPanel.add(exitBtn);

        mainPanel.add(buttonsPanel);
        mainPanel.add(Box.createVerticalGlue());

        bgPanel.add(mainPanel, new GridBagConstraints());

        // ===== actions =====
        newGameBtn.addActionListener(e -> {
            app.openNewMatch();
            dispose();
        });

        historyBtn.addActionListener(e -> {
            app.openHistory();
            dispose();
        });

        qmanBtn.addActionListener(e -> {
            app.openQuestionManager();
            dispose();
        });

        personalizationBtn.addActionListener(e -> {
            app.openPersonalization();
            dispose();
        });

        exitBtn.addActionListener(e -> confirmExit());
    }

    // ===== background panel for this view =====
    private static class BackgroundPanel extends JPanel {
        private final Image bg;

        public BackgroundPanel(Image bg) {
            this.bg = bg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (bg == null) {
                // gradient fallback
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

            // cover screen (like wallpaper)
            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            g.drawImage(bg, x, y, drawW, drawH, this);
        }
    }
}
