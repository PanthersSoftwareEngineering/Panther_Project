package view;

import controller.AppController;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * New match screen:
 * - Lets the user enter Player 1 / Player 2 names
 * - Lets the user choose difficulty (Easy / Medium / Hard)
 * - Shows info about each difficulty (lives, mines, surprises, questions)
 *
 * Validations on Start:
 * 1) Names cannot be empty
 * 2) Names cannot be the same (case-insensitive)
 * 3) Names must be at most 10 characters
 * 4) Names may contain only English letters and digits (A-Z, a-z, 0-9)
 * **/

public class NewMatchView extends BaseGameFrame {

    // ----- player fields -----
    private final JTextField p1 = new JTextField("Player A", 15);
    private final JTextField p2 = new JTextField("Player B", 15);

    // ----- difficulty buttons -----
    private final JButton easyBtn  = new JButton("Easy");
    private final JButton medBtn   = new JButton("Medium");
    private final JButton hardBtn  = new JButton("Hard");

    // ----- per-level info labels (4 lines per level) -----
    private final JLabel easyLivesLabel      = new JLabel();
    private final JLabel easyMinesLabel      = new JLabel();
    private final JLabel easySurprisesLabel  = new JLabel();
    private final JLabel easyQuestionsLabel  = new JLabel();

    private final JLabel medLivesLabel       = new JLabel();
    private final JLabel medMinesLabel       = new JLabel();
    private final JLabel medSurprisesLabel   = new JLabel();
    private final JLabel medQuestionsLabel   = new JLabel();

    private final JLabel hardLivesLabel      = new JLabel();
    private final JLabel hardMinesLabel      = new JLabel();
    private final JLabel hardSurprisesLabel  = new JLabel();
    private final JLabel hardQuestionsLabel  = new JLabel();

    /** Selected difficulty – must be chosen by the user (null by default). */
    private String selectedDifficulty = null;

    public NewMatchView(AppController app) {
        super(app, "New Match");

        if (app == null) {
            throw new IllegalArgumentException("AppController must not be null");
        }

        // ===== background =====
        Image bgImage = GameAssets.GAME_BACKGROUND;
        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // =========================================================
        // TOP SECTION: TITLE 
        // =========================================================
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(Box.createVerticalStrut(30));

        JLabel titleLabel = new JLabel("New Match");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(UIStyles.ACCENT);            
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 80)); 
        topPanel.add(titleLabel);

        topPanel.add(Box.createVerticalStrut(15));
        bgPanel.add(topPanel, BorderLayout.NORTH);

        // ================= MAIN PANEL =================
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // small vertical offset from the title
        mainPanel.add(Box.createVerticalStrut(40));

        // ===== center content (players + difficulty + buttons) =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.setMaximumSize(new Dimension(1200, Integer.MAX_VALUE));

        // ----- players row: 2 columns -----
        JPanel playersRow = new JPanel(new GridLayout(1, 2, 80, 0));
        playersRow.setOpaque(false);

        JPanel p1Col = new JPanel();
        p1Col.setOpaque(false);
        p1Col.setLayout(new BoxLayout(p1Col, BoxLayout.Y_AXIS));
        JLabel p1Label = new JLabel("Player 1 Name");
        p1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p1Label.setFont(new Font("Segoe UI", Font.BOLD, 36));
        p1Label.setForeground(Color.WHITE);
        p1Col.add(p1Label);
        p1Col.add(Box.createVerticalStrut(8));
        styleTextField(p1);
        p1.setMaximumSize(new Dimension(500, 55));
        p1Col.add(p1);

        JPanel p2Col = new JPanel();
        p2Col.setOpaque(false);
        p2Col.setLayout(new BoxLayout(p2Col, BoxLayout.Y_AXIS));
        JLabel p2Label = new JLabel("Player 2 Name");
        p2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p2Label.setFont(new Font("Segoe UI", Font.BOLD, 36));
        p2Label.setForeground(Color.WHITE);
        p2Col.add(p2Label);
        p2Col.add(Box.createVerticalStrut(8));
        styleTextField(p2);
        p2.setMaximumSize(new Dimension(500, 55));
        p2Col.add(p2);

        playersRow.add(p1Col);
        playersRow.add(p2Col);

        center.add(playersRow);
        center.add(Box.createVerticalStrut(40));

        // ----- Difficulty title -----
        JLabel diffTitle = new JLabel("Difficulty Level", SwingConstants.CENTER);
        diffTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        diffTitle.setForeground(Color.WHITE);
        diffTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        center.add(diffTitle);
        center.add(Box.createVerticalStrut(10));

        // ----- Difficulty row: Easy | Medium | Hard -----
        JPanel diffRow = new JPanel();
        diffRow.setOpaque(false);
        diffRow.setLayout(new BoxLayout(diffRow, BoxLayout.X_AXIS));

        // Easy column
        JPanel easyCol = new JPanel();
        easyCol.setOpaque(false);
        easyCol.setLayout(new BoxLayout(easyCol, BoxLayout.Y_AXIS));
        styleDifficultyButton(easyBtn);
        easyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        easyCol.add(easyBtn);
        easyCol.add(Box.createVerticalStrut(40));
        styleInfoLabel(easyLivesLabel);
        styleInfoLabel(easyMinesLabel);
        styleInfoLabel(easySurprisesLabel);
        styleInfoLabel(easyQuestionsLabel);
        easyCol.add(easyLivesLabel);
        easyCol.add(Box.createVerticalStrut(3));
        easyCol.add(easyMinesLabel);
        easyCol.add(Box.createVerticalStrut(3));
        easyCol.add(easySurprisesLabel);
        easyCol.add(Box.createVerticalStrut(3));
        easyCol.add(easyQuestionsLabel);

        // Medium column
        JPanel medCol = new JPanel();
        medCol.setOpaque(false);
        medCol.setLayout(new BoxLayout(medCol, BoxLayout.Y_AXIS));
        styleDifficultyButton(medBtn);
        medBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        medCol.add(medBtn);
        medCol.add(Box.createVerticalStrut(40));
        styleInfoLabel(medLivesLabel);
        styleInfoLabel(medMinesLabel);
        styleInfoLabel(medSurprisesLabel);
        styleInfoLabel(medQuestionsLabel);
        medCol.add(medLivesLabel);
        medCol.add(Box.createVerticalStrut(3));
        medCol.add(medMinesLabel);
        medCol.add(Box.createVerticalStrut(3));
        medCol.add(medSurprisesLabel);
        medCol.add(Box.createVerticalStrut(3));
        medCol.add(medQuestionsLabel);

        // Hard column
        JPanel hardCol = new JPanel();
        hardCol.setOpaque(false);
        hardCol.setLayout(new BoxLayout(hardCol, BoxLayout.Y_AXIS));
        styleDifficultyButton(hardBtn);
        hardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        hardCol.add(hardBtn);
        hardCol.add(Box.createVerticalStrut(40));
        styleInfoLabel(hardLivesLabel);
        styleInfoLabel(hardMinesLabel);
        styleInfoLabel(hardSurprisesLabel);
        styleInfoLabel(hardQuestionsLabel);
        hardCol.add(hardLivesLabel);
        hardCol.add(Box.createVerticalStrut(3));
        hardCol.add(hardMinesLabel);
        hardCol.add(Box.createVerticalStrut(3));
        hardCol.add(hardSurprisesLabel);
        hardCol.add(Box.createVerticalStrut(3));
        hardCol.add(hardQuestionsLabel);

        diffRow.add(Box.createHorizontalGlue());
        diffRow.add(easyCol);
        diffRow.add(Box.createHorizontalStrut(40));
        diffRow.add(medCol);
        diffRow.add(Box.createHorizontalStrut(40));
        diffRow.add(hardCol);
        diffRow.add(Box.createHorizontalGlue());

        center.add(diffRow);
        center.add(Box.createVerticalStrut(70));

        // ----- bottom buttons row: Back | Start -----
        JPanel bottomButtons = new JPanel();
        bottomButtons.setOpaque(false);
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.X_AXIS));
        bottomButtons.setMaximumSize(new Dimension(1200, 120));

        ButtonStyled backBtn  = new ButtonStyled("Back");
        ButtonStyled startBtn = new ButtonStyled("Start");

        bottomButtons.add(backBtn);
        bottomButtons.add(Box.createHorizontalGlue());
        bottomButtons.add(startBtn);

        center.add(bottomButtons);
        center.add(Box.createVerticalStrut(50));

       
        mainPanel.add(center);
        mainPanel.add(Box.createVerticalGlue());
        bgPanel.add(mainPanel, BorderLayout.CENTER);

        // difficulty selection + info logic
        initDifficultyLogic();

        // actions
        wireActions(startBtn, backBtn);
    }

    // ===================== difficulty logic =====================

    private void initDifficultyLogic() {
        attachDifficultyHover(easyBtn, "EASY");
        attachDifficultyHover(medBtn,  "MEDIUM");
        attachDifficultyHover(hardBtn, "HARD");

        easyBtn.addActionListener(e -> {
            selectedDifficulty = "EASY";
            refreshDifficultyStyles();
            updateDifficultyInfo();
        });
        medBtn.addActionListener(e -> {
            selectedDifficulty = "MEDIUM";
            refreshDifficultyStyles();
            updateDifficultyInfo();
        });
        hardBtn.addActionListener(e -> {
            selectedDifficulty = "HARD";
            refreshDifficultyStyles();
            updateDifficultyInfo();
        });

        // initial styles (no selection yet)
        refreshDifficultyStyles();
    }

    private void styleDifficultyButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 32));
        btn.setForeground(Color.WHITE);

        Dimension d = new Dimension(260, 100);
        btn.setPreferredSize(d);
        btn.setMinimumSize(d);
        btn.setMaximumSize(d);

        btn.setBorder(new LineBorder(Color.LIGHT_GRAY, 4, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void attachDifficultyHover(JButton btn, String level) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!level.equals(selectedDifficulty)) {
                    btn.setBackground(new Color(45, 45, 45));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                refreshDifficultyStyles();
            }
        });
    }

    private void refreshDifficultyStyles() {
        Color selectedBg       = new Color(15, 15, 15);
        Color unselectedBg     = new Color(25, 25, 25);
        Color selectedBorder   = UIStyles.ACCENT;          
        Color unselectedBorder = new Color(210, 210, 210);
        int borderWidth = 4;

        if ("EASY".equals(selectedDifficulty)) {
            easyBtn.setBackground(selectedBg);
            easyBtn.setBorder(new LineBorder(selectedBorder, borderWidth, true));
        } else {
            easyBtn.setBackground(unselectedBg);
            easyBtn.setBorder(new LineBorder(unselectedBorder, borderWidth, true));
        }

        if ("MEDIUM".equals(selectedDifficulty)) {
            medBtn.setBackground(selectedBg);
            medBtn.setBorder(new LineBorder(selectedBorder, borderWidth, true));
        } else {
            medBtn.setBackground(unselectedBg);
            medBtn.setBorder(new LineBorder(unselectedBorder, borderWidth, true));
        }

        if ("HARD".equals(selectedDifficulty)) {
            hardBtn.setBackground(selectedBg);
            hardBtn.setBorder(new LineBorder(selectedBorder, borderWidth, true));
        } else {
            hardBtn.setBackground(unselectedBg);
            hardBtn.setBorder(new LineBorder(unselectedBorder, borderWidth, true));
        }
    }

    /** Set all 4 lines for the selected difficulty only */
    private void updateDifficultyInfo() {
        JLabel[] allLabels = {
                easyLivesLabel, easyMinesLabel, easySurprisesLabel, easyQuestionsLabel,
                medLivesLabel,  medMinesLabel,  medSurprisesLabel,  medQuestionsLabel,
                hardLivesLabel, hardMinesLabel, hardSurprisesLabel, hardQuestionsLabel
        };
        for (JLabel lbl : allLabels) {
            lbl.setText(" ");
        }

        switch (selectedDifficulty) {
            case "EASY" -> {
                easyLivesLabel.setText("Number of Lives: 10");
                easyMinesLabel.setText("Number of Mines: 10");
                easySurprisesLabel.setText("Number of Surprises: 2");
                easyQuestionsLabel.setText("Number of Questions: 6");
            }
            case "MEDIUM" -> {
                medLivesLabel.setText("Number of Lives: 8");
                medMinesLabel.setText("Number of Mines: 26");
                medSurprisesLabel.setText("Number of Surprises: 3");
                medQuestionsLabel.setText("Number of Questions: 7");
            }
            case "HARD" -> {
                hardLivesLabel.setText("Number of Lives: 6");
                hardMinesLabel.setText("Number of Mines: 44");
                hardSurprisesLabel.setText("Number of Surprises: 4");
                hardQuestionsLabel.setText("Number of Questions: 11");
            }
        }
    }

    // ===================== wiring buttons =====================

    private void wireActions(ButtonStyled startBtn, ButtonStyled backBtn) {
        startBtn.addActionListener(e -> {
            String name1 = normName(p1.getText());
            String name2 = normName(p2.getText());

            boolean p1Empty = name1.isEmpty();
            boolean p2Empty = name2.isEmpty();

            // 1) Names cannot be empty
            if (p1Empty || p2Empty) {
                StyledAlertDialog.show(
                        this,
                        "Player Names Required",
                        p1Empty && p2Empty ? "Both player names are empty.\nPlease fill in both names."
                                : (p1Empty ? "Player 1 name is empty.\nPlease fill in Player 1 name."
                                : "Player 2 name is empty.\nPlease fill in Player 2 name."),
                        true
                );
                if (p1Empty) p1.requestFocusInWindow();
                else p2.requestFocusInWindow();
                return;
            }

            // 2) Names must be at most 10 characters
            if (name1.length() > 10) {
                StyledAlertDialog.show(this, "Invalid Player 1 Name",
                        "Player 1 name must be at most 10 characters.", true);
                p1.requestFocusInWindow();
                p1.selectAll();
                return;
            }

            if (name2.length() > 10) {
                StyledAlertDialog.show(this, "Invalid Player 2 Name",
                        "Player 2 name must be at most 10 characters.", true);
                p2.requestFocusInWindow();
                p2.selectAll();
                return;
            }

            // 3) Names may contain only English letters and digits
            if (!isOnlyEnglishLettersAndDigits(name1)) {
                StyledAlertDialog.show(this, "Invalid Player 1 Name",
                        "Player 1 name may contain only English letters and digits.", true);
                p1.requestFocusInWindow();
                p1.selectAll();
                return;
            }

            if (!isOnlyEnglishLettersAndDigits(name2)) {
                StyledAlertDialog.show(this, "Invalid Player 2 Name",
                        "Player 2 name may contain only English letters and digits.", true);
                p2.requestFocusInWindow();
                p2.selectAll();
                return;
            }

            // 4) Names cannot be the same (case-insensitive)
            if (name1.equalsIgnoreCase(name2)) {
                StyledAlertDialog.show(
                        this,
                        "Invalid Names",
                        "Player 1 and Player 2 names cannot be the same.\nPlease enter two different names.",
                        true
                );
                p2.requestFocusInWindow();
                p2.selectAll();
                return;
            }

            // Difficulty must be selected
            if (selectedDifficulty == null) {
                StyledAlertDialog.show(
                        this,
                        "Difficulty Required",
                        "Please choose a difficulty level before starting the game.",
                        true
                );
                return;
            }

            try {
                app.onStart(name1, name2, selectedDifficulty);
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                StyledAlertDialog.show(
                        this,
                        "Error",
                        "An error occurred while starting the game:\n" + ex.getMessage(),
                        true
                );
            }
        });

        backBtn.addActionListener(e -> {
            try {
                dispose();
                app.showMainMenu();
            } catch (Exception ex) {
                ex.printStackTrace();
                StyledAlertDialog.show(
                        this,
                        "Error",
                        "An error occurred while returning to the main menu:\n" + ex.getMessage(),
                        true
                );
            }
        });
    }

    // ===================== small helpers =====================

    /** Normalize spaces: trim and collapse multiple spaces to one */
    private static String normName(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    /** Returns true only if the string is made of English letters and digits (A-Z, a-z, 0-9) */
    private static boolean isOnlyEnglishLettersAndDigits(String s) {
        return s != null && s.matches("^[A-Za-z0-9]+( [A-Za-z0-9]+)*$");
    }

    private void styleTextField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        f.setPreferredSize(new Dimension(500, 55));
        f.setMaximumSize(new Dimension(500, 55));
    }

    private void styleInfoLabel(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lbl.setForeground(Color.WHITE);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension infoSize = new Dimension(260, 22);
        lbl.setPreferredSize(infoSize);
        lbl.setMinimumSize(infoSize);
        lbl.setMaximumSize(infoSize);
    }

    // ===================== background panel =====================

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

    // ===================== big button (Back / Start) =====================

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

            // ✅ Always use current accent (in case theme changes while open)
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(UIStyles.ACCENT);
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
