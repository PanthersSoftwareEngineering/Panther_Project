package view;

import controller.AppController;
import controller.MatchController;
import model.SysData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Game screen with two boards (P1 left, P2 right) + HUD.
 *
 * Uses the "good boards" rendering:
 *  - CellButton + CellStyle.colorForSymbol(sym, playerIdx)
 *  - Correct symbol display, correct base colors, correct text colors
 *
 * Requires (as you already have):
 *  - GameAssets, BackgroundPanel, UIStyles, CellButton, CellStyle, Toast, QuestionDialog
 */
public class GameViewTwoBoards extends BaseGameFrame implements QuestionUI {

    private final MatchController ctrl;
    private final AppController app;

    // HUD labels
    private final JLabel lblP1 = new JLabel();
    private final JLabel lblP2 = new JLabel();
    private final JLabel lblLives  = new JLabel();
    private final JLabel lblPoints = new JLabel();
    private final JLabel lblTimer  = new JLabel();
    private final JLabel lblActive = new JLabel();
    private final JLabel lblDifficulty = new JLabel();

    // ACTIVE chips (badges)
    private final JLabel chipP1Active = new JLabel("ACTIVE", SwingConstants.CENTER);
    private final JLabel chipP2Active = new JLabel("ACTIVE", SwingConstants.CENTER);

    private final JPanel board1 = new JPanel();
    private final JPanel board2 = new JPanel();

    private CellButton[][] btn1, btn2;

    private Timer timer;
    private boolean endSequenceStarted = false;

    public GameViewTwoBoards(MatchController ctrl, AppController app) {
        super(app, "Minesweeper - Match");
        this.ctrl = ctrl;
        this.app  = app;

        ctrl.setQuestionUI(this);

        // ============================================================
        // Background (donor style)
        // ============================================================
        BackgroundPanel bg = new BackgroundPanel(GameAssets.MATCH_BACKGROUND);
        bg.setLayout(new BorderLayout(8, 8));
        setContentPane(bg);

        // (אם BaseGameFrame שלך כולל שכבת Toast) – להשאיר
        // אם אין לך פונקציה כזו ב-BaseGameFrame, פשוט תמחק את השורה הזו.
        installToastLayer();

        // ============================================================
        // HUD: Row 1 (BIG Difficulty) + Row 2 (Stats)
        // ============================================================
        JPanel hud = UIStyles.translucentPanel(new BorderLayout(), UIStyles.HUD_PANEL_BG);
        hud.setBorder(UIStyles.pad(12, 16, 12, 16));

        // ---- Row 1: Difficulty (BIG) ----
        UIStyles.styleTitle(lblDifficulty);
        lblDifficulty.setHorizontalAlignment(SwingConstants.LEFT);
        lblDifficulty.setText("Difficulty: " + ctrl.getDiff());

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        row1.add(lblDifficulty, BorderLayout.WEST);

        // ---- Row 2: Lives / Points / Time / Active / Back ----
        UIStyles.styleHudLabel(lblLives);
        UIStyles.styleHudLabel(lblPoints);
        UIStyles.styleHudLabel(lblTimer);
        UIStyles.styleHudLabel(lblActive);

        BaseGameFrame.RoundedButton back =
                new BaseGameFrame.RoundedButton("Back to Main", 260, 64, 22);

        back.addActionListener(e -> {
            if (timer != null) timer.stop();
            dispose();
            if (app != null) app.showMainMenu();
        });


        JPanel row2 = new JPanel(new GridLayout(1, 5, 14, 0));
        row2.setOpaque(false);
        row2.add(lblLives);
        row2.add(lblPoints);
        row2.add(lblTimer);
        row2.add(lblActive);
        row2.add(back);

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


        // ============================================================
        // Board containers (Name + ACTIVE chip)  [הגרסה הטובה שלך]
        // ============================================================
        lblP1.setHorizontalAlignment(SwingConstants.CENTER);
        lblP2.setHorizontalAlignment(SwingConstants.CENTER);
        UIStyles.styleTitle(lblP1);
        UIStyles.styleTitle(lblP2);

        JPanel left  = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());
        left.setOpaque(false);
        right.setOpaque(false);

        // chips config
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


        // ============================================================
        // Build + refresh
        // ============================================================
        buildBoards();
        refreshAll();

        timer = new Timer(1000, e -> {
            lblTimer.setText("Time: " + UIStyles.formatTimeMMSS(ctrl.getElapsedSeconds()));
            lblDifficulty.setText("Difficulty: " + ctrl.getDiff());
        });
        timer.start();

        // full screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // ============================================================
    // BUILD BOARDS (CellButton + proper style)
    // ============================================================
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

                // Player 1 board click
                a.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!ctrl.isPlayer1Active()) return;
                        final int playerIdx = 0;

                        if (ctrl.isQuestionUsed(0, rr, cc) || ctrl.isSurpriseUsed(0, rr, cc)) {
                            Toast.show(GameViewTwoBoards.this, "תא זה כבר נבחר בעבר!");
                            return;
                        }

                        if (SwingUtilities.isRightMouseButton(e)) {
                            ctrl.toggleFlag(playerIdx, rr, cc);
                        } else {
                            if (!ctrl.tryInteract(playerIdx, rr, cc)) {
                                if (ctrl.isRevealed(playerIdx, rr, cc)) return;
                                ctrl.reveal(rr, cc);
                            } else {
                                String msg = ctrl.consumeLastSurpriseMessage();
                                if (msg != null) Toast.show(GameViewTwoBoards.this, msg);
                            }
                        }

                        refreshAll();
                        endCheck();
                    }
                });

                // Player 2 board click
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (ctrl.isPlayer1Active()) return;
                        final int playerIdx = 1;

                        if (ctrl.isQuestionUsed(1, rr, cc) || ctrl.isSurpriseUsed(1, rr, cc)) {
                            Toast.show(GameViewTwoBoards.this, "תא זה כבר נבחר בעבר!");
                            return;
                        }

                        if (SwingUtilities.isRightMouseButton(e)) {
                            ctrl.toggleFlag(playerIdx, rr, cc);
                        } else {
                            if (!ctrl.tryInteract(playerIdx, rr, cc)) {
                                if (ctrl.isRevealed(playerIdx, rr, cc)) return;
                                ctrl.reveal(rr, cc);
                            } else {
                                String msg = ctrl.consumeLastSurpriseMessage();
                                if (msg != null) Toast.show(GameViewTwoBoards.this, msg);
                            }
                        }

                        refreshAll();
                        endCheck();
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

    // ============================================================
    // REFRESH ALL (symbols + correct colors)
    // ============================================================
    private void refreshAll() {
        boolean p1Active = ctrl.isPlayer1Active();

        // names
        lblP1.setText(ctrl.getP1());
        lblP2.setText(ctrl.getP2());

        // HUD
        lblLives.setText("Lives: " + ctrl.getLives());
        lblPoints.setText("Points: " + ctrl.getPoints());
        lblTimer.setText("Time: " + UIStyles.formatTimeMMSS(ctrl.getElapsedSeconds()));
        lblActive.setText("Active: " + (p1Active ? ctrl.getP1() : ctrl.getP2()));
        lblDifficulty.setText("Difficulty: " + ctrl.getDiff());

        // ACTIVE chips
        chipP1Active.setVisible(p1Active);
        chipP2Active.setVisible(!p1Active);
        chipP1Active.setBackground(UIStyles.CHIP_BG_ACTIVE_P1);
        chipP2Active.setBackground(UIStyles.CHIP_BG_ACTIVE_P2);

        String[][] g1 = ctrl.symbolsOfBoard(0);
        String[][] g2 = ctrl.symbolsOfBoard(1);

        // Board 1
        for (int r = 0; r < g1.length; r++) {
            for (int c = 0; c < g1[0].length; c++) {
                CellButton btn = btn1[r][c];
                String sym = g1[r][c];
                btn.setText(sym);

                btn.setBaseColor(CellStyle.colorForSymbol(sym, 0));
                btn.setForeground(CellStyle.textColorForSymbol(sym));

                if (ctrl.isQuestionUsed(0, r, c) || ctrl.isSurpriseUsed(0, r, c)) {
                    btn.setBaseColor(CellStyle.USED);
                    btn.setForeground(Color.WHITE);
                }
            }
        }

        // Board 2
        for (int r = 0; r < g2.length; r++) {
            for (int c = 0; c < g2[0].length; c++) {
                CellButton btn = btn2[r][c];
                String sym = g2[r][c];
                btn.setText(sym);

                btn.setBaseColor(CellStyle.colorForSymbol(sym, 1));
                btn.setForeground(CellStyle.textColorForSymbol(sym));

                if (ctrl.isQuestionUsed(1, r, c) || ctrl.isSurpriseUsed(1, r, c)) {
                    btn.setBaseColor(CellStyle.USED);
                    btn.setForeground(Color.WHITE);
                }
            }
        }

        // enable/disable boards
        setPanelEnabled(board1, p1Active);
        setPanelEnabled(board2, !p1Active);

        repaint();
    }

    private static void setPanelEnabled(Container p, boolean enabled) {
        for (Component c : p.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container cc)
                setPanelEnabled(cc, enabled);
        }
    }

    // ============================================================
    // END CHECK (show end screen after 10 sec)
    // ============================================================
    private void endCheck() {
        if (!ctrl.isFinished()) return;
        if (endSequenceStarted) return;
        endSequenceStarted = true;

        if (timer != null) timer.stop();
        refreshAll();

        Timer delay = new Timer(10_000, e -> {
            dispose();
            SysData.GameRecord rec = ctrl.getLastRecord();
            if (app != null && rec != null) {
                app.openEndScreen(rec);
            }
        });
        delay.setRepeats(false);
        delay.start();
    }

    // ============================================================
    // QUESTION UI (gold dialog)
    // ============================================================
    @Override
    public int ask(QuestionDTO q) {
        QuestionDialog dialog = new QuestionDialog(this, q);
        return dialog.showDialog();
    }

    @Override
    public void showSelf() {
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
