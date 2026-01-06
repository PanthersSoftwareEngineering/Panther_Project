package view;

import controller.AppController;
import controller.MatchController;
import model.MatchListener;
import model.MatchSnapshot;
import model.SysData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * GameViewTwoBoards is the main match screen for a 2-player game
 * Key responsibilities:
 * - Display two boards side-by-side (Player 1 left, Player 2 right)
 * - Show HUD information (names, lives, points, timer, active player, difficulty)
 * - Forward user actions (left/right click) to MatchController
 * - React to model updates using the Observer pattern (MatchListener -> MatchSnapshot)
 */
public class GameViewTwoBoards extends BaseGameFrame implements QuestionUI, MatchListener {

    private final MatchController ctrl;
    private final AppController app;

    private final JLabel lblP1 = new JLabel();
    private final JLabel lblP2 = new JLabel();

    // NOTE: we keep lblLives but we won't use it directly in the HUD anymore.
    private final JLabel lblLives  = new JLabel();
    private final JLabel lblPoints = new JLabel();
    private final JLabel lblTimer  = new JLabel();
    private final JLabel lblActive = new JLabel();
    private final JLabel lblDifficulty = new JLabel();

    private final JLabel chipP1Active = new JLabel("ACTIVE", SwingConstants.CENTER);
    private final JLabel chipP2Active = new JLabel("ACTIVE", SwingConstants.CENTER);

    private final JPanel board1 = new JPanel();
    private final JPanel board2 = new JPanel();

    private CellButton[][] btn1, btn2;

    private Timer timer;
    private boolean endSequenceStarted = false;

    // -------------------------
    // Hearts UI (unchanged)
    // -------------------------
    private static final int MAX_LIVES_UI = 10;
    private final JLabel lblLivesTitle = new JLabel("Lives:");
    private final HeartsPanel heartsPanel = new HeartsPanel(MAX_LIVES_UI, MAX_LIVES_UI);
    private int lastLivesSeen = MAX_LIVES_UI;

    // -------------------------
    // NEW: Mine explosion state
    // -------------------------
    private String[][] prevG1 = null;
    private String[][] prevG2 = null;

    private boolean lossBombCascadeStarted = false;
    private Timer bombCascadeTimer = null;

    public GameViewTwoBoards(MatchController ctrl, AppController app) {
        super(app, "Minesweeper - Match");
        this.ctrl = ctrl;
        this.app  = app;

        ctrl.setQuestionUI(this);
        ctrl.addMatchListener(this);

        BackgroundPanel bg = new BackgroundPanel(GameAssets.MATCH_BACKGROUND);
        bg.setLayout(new BorderLayout(8, 8));
        setContentPane(bg);

        installToastLayer();

        JPanel hud = UIStyles.translucentPanel(new BorderLayout(), UIStyles.HUD_PANEL_BG);
        hud.setBorder(UIStyles.pad(12, 16, 12, 16));

        UIStyles.styleTitle(lblDifficulty);
        lblDifficulty.setHorizontalAlignment(SwingConstants.LEFT);
        lblDifficulty.setText("Difficulty: " + ctrl.getDiff());

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        row1.add(lblDifficulty, BorderLayout.WEST);

        UIStyles.styleHudLabel(lblLives);
        UIStyles.styleHudLabel(lblPoints);
        UIStyles.styleHudLabel(lblTimer);
        UIStyles.styleHudLabel(lblActive);

        UIStyles.styleHudLabel(lblLivesTitle);

        JPanel livesHud = new JPanel();
        livesHud.setOpaque(false);
        livesHud.setLayout(new BoxLayout(livesHud, BoxLayout.X_AXIS));
        livesHud.add(lblLivesTitle);
        livesHud.add(Box.createHorizontalStrut(10));
        livesHud.add(heartsPanel);

        BaseGameFrame.RoundedButton back =
                new BaseGameFrame.RoundedButton("Back to Main", 260, 64, 22);

        back.addActionListener(e -> {
            if (timer != null) timer.stop();
            ctrl.removeMatchListener(this);

            // stop cascade timer if running
            if (bombCascadeTimer != null) bombCascadeTimer.stop();

            dispose();
            if (app != null) app.showMainMenu();
        });

        // Second HUD row: lives(hearts), points, timer, active player, and the back button
        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, 14);

        gc.gridx = 0;
        gc.weightx = 2.5;
        row2.add(livesHud, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        row2.add(lblPoints, gc);

        gc.gridx = 2;
        gc.weightx = 1.0;
        row2.add(lblTimer, gc);

        gc.gridx = 3;
        gc.weightx = 1.0;
        row2.add(lblActive, gc);

        gc.gridx = 4;
        gc.weightx = 0.0;
        gc.insets = new Insets(0, 0, 0, 0);
        row2.add(back, gc);

        JPanel hudInner = new JPanel();
        hudInner.setOpaque(false);
        hudInner.setLayout(new BoxLayout(hudInner, BoxLayout.Y_AXIS));
        hudInner.add(row1);
        hudInner.add(Box.createVerticalStrut(8));
        hudInner.add(row2);

        hud.add(hudInner, BorderLayout.CENTER);

        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.setOpaque(false);
        topWrap.setBorder(UIStyles.pad(8, 8, 0, 8));
        topWrap.add(hud, BorderLayout.CENTER);

        bg.add(topWrap, BorderLayout.NORTH);

        lblP1.setHorizontalAlignment(SwingConstants.CENTER);
        lblP2.setHorizontalAlignment(SwingConstants.CENTER);
        UIStyles.styleTitle(lblP1);
        UIStyles.styleTitle(lblP2);

        JPanel left  = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());
        left.setOpaque(false);
        right.setOpaque(false);

        chipP1Active.setOpaque(true);
        chipP1Active.setForeground(UIStyles.CHIP_TEXT);
        chipP1Active.setFont(UIStyles.HUD_FONT_SMALL);
        chipP1Active.setBorder(UIStyles.pad(4, 10, 4, 10));

        chipP2Active.setOpaque(true);
        chipP2Active.setForeground(UIStyles.CHIP_TEXT);
        chipP2Active.setFont(UIStyles.HUD_FONT_SMALL);
        chipP2Active.setBorder(UIStyles.pad(4, 10, 4, 10));

        JPanel head1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        head1.setOpaque(false);
        head1.add(lblP1);
        head1.add(chipP1Active);

        JPanel head2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        head2.setOpaque(false);
        head2.add(lblP2);
        head2.add(chipP2Active);

        left.add(head1, BorderLayout.NORTH);
        right.add(head2, BorderLayout.NORTH);

        board1.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
        board2.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2));
        board1.setOpaque(false);
        board2.setOpaque(false);

        left.add(board1, BorderLayout.CENTER);
        right.add(board2, BorderLayout.CENTER);

        JPanel both = new JPanel(new GridLayout(1, 2, 8, 8));
        both.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        both.setOpaque(false);
        both.add(left);
        both.add(right);

        bg.add(both, BorderLayout.CENTER);

        buildBoards();

        timer = new Timer(1000, e -> {
            lblTimer.setText("Time: " + UIStyles.formatTimeMMSS(ctrl.getElapsedSeconds()));
            lblDifficulty.setText("Difficulty: " + ctrl.getDiff());
        });
        timer.start();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void onMatchChanged(MatchSnapshot s) {
        SwingUtilities.invokeLater(() -> {
            refreshFromSnapshot(s);
            endCheck(s);
        });
    }

    private void buildBoards() {
        int R = ctrl.rows(), C = ctrl.cols();
        btn1 = new CellButton[R][C];
        btn2 = new CellButton[R][C];

        board1.setLayout(new GridLayout(R, C, 6, 6));
        board2.setLayout(new GridLayout(R, C, 6, 6));
        board1.removeAll();
        board2.removeAll();

        Dimension cellSize = new Dimension(44, 44);

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                CellButton a = new CellButton("·", CellStyle.P1_BASE);
                CellButton b = new CellButton("·", CellStyle.P2_BASE);
                a.setPreferredSize(cellSize);
                b.setPreferredSize(cellSize);

                final int rr = r, cc = c;

                a.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!ctrl.isPlayer1Active()) return;
                        handleClick(0, rr, cc, e);
                    }
                });

                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (ctrl.isPlayer1Active()) return;
                        handleClick(1, rr, cc, e);
                    }
                });

                btn1[r][c] = a;
                btn2[r][c] = b;

                board1.add(a);
                board2.add(b);
            }
        }

        board1.revalidate();
        board2.revalidate();
        board1.repaint();
        board2.repaint();
    }

    private void handleClick(int playerIdx, int r, int c, MouseEvent e) {
        if (ctrl.isQuestionUsed(playerIdx, r, c) || ctrl.isSurpriseUsed(playerIdx, r, c)) {
            Toast.show(this, "Cell Already Chosen!");
            return;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            ctrl.toggleFlag(playerIdx, r, c);
            return;
        }

        if (ctrl.isFlagged(playerIdx, r, c)) {
            Toast.show(this, "Remove flag first (Right-click) to reveal.");
            return;
        }

        if (ctrl.tryInteract(playerIdx, r, c)) {
            showLastInteractionToast();
            return;
        }

        if (!ctrl.isRevealed(playerIdx, r, c)) {
            ctrl.reveal(r, c);
        }

        showLastInteractionToast();
    }

    private void showLastInteractionToast() {
        String msg = ctrl.consumeLastInteractionMessage();
        if (msg == null) return;

        String details =
                " cost:" + fmtDelta(-ctrl.getLastActivationCost(), "pts") +
                        " effect:" + fmtDelta(ctrl.getLastEffectPoints(), "pts") + "," + fmtDelta(ctrl.getLastEffectLives(), "❤") +
                        " net:" + fmtDelta(ctrl.getLastNetPoints(), "pts") + "," + fmtDelta(ctrl.getLastNetLives(), "❤");

        Toast.show(this, msg + "  " + details);
    }

    private String fmtDelta(int v, String unit) {
        if (v > 0) return "+" + v + " " + unit;
        if (v < 0) return v + " " + unit;
        return "0 " + unit;
    }

    private void refreshFromSnapshot(MatchSnapshot s) {
        boolean p1Active = (s.activeIndex() == 0);
        boolean finished = s.finished();

        lblP1.setText(s.p1());
        lblP2.setText(s.p2());

        lblLives.setText("Lives: " + s.lives());

        // hearts update (unchanged)
        int newLives = s.lives();
        if (newLives != lastLivesSeen) {
            heartsPanel.setLives(newLives);
            lastLivesSeen = newLives;
        }

        lblPoints.setText("Points: " + s.points());
        lblTimer.setText("Time: " + UIStyles.formatTimeMMSS(ctrl.getElapsedSeconds()));
        lblActive.setText("Active: " + (p1Active ? s.p1() : s.p2()));
        lblDifficulty.setText("Difficulty: " + s.level().name());

        chipP1Active.setVisible(!finished && p1Active);
        chipP2Active.setVisible(!finished && !p1Active);
        chipP1Active.setBackground(UIStyles.CHIP_BG_ACTIVE_P1);
        chipP2Active.setBackground(UIStyles.CHIP_BG_ACTIVE_P2);

        String[][] g1 = s.boardP1();
        String[][] g2 = s.boardP2();

        // ---- Player 1 board refresh + mine explosions ----
        for (int r = 0; r < g1.length; r++) {
            for (int c = 0; c < g1[0].length; c++) {
                CellButton btn = btn1[r][c];
                String sym = g1[r][c];
                btn.setText(sym);
                

                // Trigger explosion if mine becomes visible now (transition)
                String old = (prevG1 == null) ? null : prevG1[r][c];
                if (isMineSymbol(sym) && !isMineSymbol(old)) {
                    btn.startExplosion();
                }
                if (isSurpriseSymbol(sym) && !isSurpriseSymbol(old)) {
                    btn.startSparkle();
                }

                boolean isUsed = ctrl.isQuestionUsed(0, r, c) || ctrl.isSurpriseUsed(0, r, c);

                boolean isQuestion = sym.equals("❓") || sym.equals("?");
                boolean isSurprise = sym.equals("🎁");
                boolean isSpecialPending = (isQuestion || isSurprise) && !isUsed;

                if (isUsed) {
                    btn.setBaseColor(CellStyle.USED);
                    btn.setForeground(Color.WHITE);
                } else if (!sym.equals("·") && !isSpecialPending) {
                    btn.setBaseColor(new Color(50, 65, 120));
                    btn.setForeground(CellStyle.textColorForSymbol(sym));
                } else {
                    btn.setBaseColor(CellStyle.colorForSymbol(sym, 0));
                    btn.setForeground(CellStyle.textColorForSymbol(sym));
                }
            }
        }

        // ---- Player 2 board refresh + mine explosions ----
        for (int r = 0; r < g2.length; r++) {
            for (int c = 0; c < g2[0].length; c++) {
                CellButton btn = btn2[r][c];
                String sym = g2[r][c];
                btn.setText(sym);

                String old = (prevG2 == null) ? null : prevG2[r][c];
                if (isMineSymbol(sym) && !isMineSymbol(old)) {
                    btn.startExplosion();
                }
                if (isSurpriseSymbol(sym) && !isSurpriseSymbol(old)) {
                    btn.startSparkle();
                }

                boolean isUsed = ctrl.isQuestionUsed(1, r, c) || ctrl.isSurpriseUsed(1, r, c);

                boolean isQuestion = sym.equals("❓") || sym.equals("?");
                boolean isSurprise = sym.equals("🎁");
                boolean isSpecialPending = (isQuestion || isSurprise) && !isUsed;

                if (isUsed) {
                    btn.setBaseColor(CellStyle.USED);
                    btn.setForeground(Color.WHITE);
                } else if (!sym.equals("·") && !isSpecialPending) {
                    btn.setBaseColor(new Color(60, 110, 80));
                    btn.setForeground(CellStyle.textColorForSymbol(sym));
                } else {
                    btn.setBaseColor(CellStyle.colorForSymbol(sym, 1));
                    btn.setForeground(CellStyle.textColorForSymbol(sym));
                }
            }
        }

        // Save current snapshot grids for transition detection next time
        prevG1 = copyGrid(g1);
        prevG2 = copyGrid(g2);

        setPanelEnabled(board1, finished || p1Active);
        setPanelEnabled(board2, finished || !p1Active);

        // If loss: explode all bombs one after another
        if (finished && s.lives() == 0 && !lossBombCascadeStarted) {
            lossBombCascadeStarted = true;
            startLossBombCascade(g1, g2);
        }

        repaint();
    }

    private void startLossBombCascade(String[][] g1, String[][] g2) {
        List<CellButton> bombs = new ArrayList<>();

        for (int r = 0; r < g1.length; r++) {
            for (int c = 0; c < g1[0].length; c++) {
                if (isMineSymbol(g1[r][c])) bombs.add(btn1[r][c]);
            }
        }
        for (int r = 0; r < g2.length; r++) {
            for (int c = 0; c < g2[0].length; c++) {
                if (isMineSymbol(g2[r][c])) bombs.add(btn2[r][c]);
            }
        }

        if (bombs.isEmpty()) return;

        final int[] idx = {0};

        if (bombCascadeTimer != null) bombCascadeTimer.stop();

        bombCascadeTimer = new Timer(120, e -> {
            if (idx[0] >= bombs.size()) {
                bombCascadeTimer.stop();
                return;
            }
            bombs.get(idx[0]).startExplosion();
            idx[0]++;
        });
        bombCascadeTimer.setRepeats(true);
        bombCascadeTimer.start();
    }

    private static boolean isMineSymbol(String sym) {
        if (sym == null) return false;
        String s = sym.trim();
        // Your mine symbol in the UI is usually "💣"
        // Extra options just in case
        return s.equals("💣") || s.equalsIgnoreCase("M") || s.equals("*");
    }
    
    private static boolean isSurpriseSymbol(String sym) {
        if (sym == null) return false;
        return sym.trim().equals("🎁");
    }

    private static String[][] copyGrid(String[][] g) {
        if (g == null) return null;
        String[][] out = new String[g.length][];
        for (int i = 0; i < g.length; i++) {
            out[i] = new String[g[i].length];
            System.arraycopy(g[i], 0, out[i], 0, g[i].length);
        }
        return out;
    }

    private static void setPanelEnabled(Container p, boolean enabled) {
        for (Component c : p.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container cc) setPanelEnabled(cc, enabled);
        }
    }

    private void endCheck(MatchSnapshot s) {
        if (!s.finished()) return;
        if (endSequenceStarted) return;
        endSequenceStarted = true;

        if (timer != null) timer.stop();

        Timer delay = new Timer(2_000, e -> {
            ctrl.removeMatchListener(this);

            // stop cascade timer if running
            if (bombCascadeTimer != null) bombCascadeTimer.stop();

            dispose();

            SysData.GameRecord rec = ctrl.getLastRecord();
            if (app != null && rec != null) {
                app.openEndScreen(rec);
            }
        });
        delay.setRepeats(false);
        delay.start();
    }

    @Override
    public int ask(QuestionDTO q) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        int correct = ctrl.getLastQuestionCorrectIndex();
        QuestionDialog dialog = new QuestionDialog(owner, q, correct);
        return dialog.showDialog();
    }

    @Override
    public boolean confirmActivation(String kindLabel, int costPoints) {
        String msg =
                "Do you want to activate this " + kindLabel + " cell?\n" +
                        "Activation cost: " + costPoints + " points\n" +
                        "After activation, you may gain/lose points and hearts.";

        int res = StyledConfirmDialog.show(this, msg, JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.OK_OPTION;
    }

    @Override
    public void showSelf() {
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
