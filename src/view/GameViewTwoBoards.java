package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import controller.AppController;
import controller.MatchController;
import model.SysData;

public class GameViewTwoBoards extends JFrame implements QuestionUI {

    private final MatchController ctrl;
    private final AppController app;

    private final JLabel lblP1 = new JLabel();
    private final JLabel lblP2 = new JLabel();
    private final JLabel lblLives  = new JLabel();
    private final JLabel lblPoints = new JLabel();
    private final JLabel lblTimer  = new JLabel();
    private final JLabel lblActive = new JLabel();

    private final JPanel board1 = new JPanel();
    private final JPanel board2 = new JPanel();

    private JButton[][] btn1, btn2;

    private Timer timer;
    /** Was end-of-game sequence already started (to avoid duplicates)? */
    private boolean endSequenceStarted = false;
    public GameViewTwoBoards(MatchController ctrl, AppController app) {
        super("Minesweeper - Match");
        this.ctrl = ctrl;
        this.app  = app;

        ctrl.setQuestionUI(this);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // ---------- HUD ----------
        JPanel top = new JPanel(new GridLayout(2, 4, 8, 4));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JButton back = new JButton("Back to Main");
        back.addActionListener(e -> {
            timer.stop();
            dispose();
            if (app != null) app.showMainMenu();
        });

        top.add(lblLives);
        top.add(lblPoints);
        top.add(lblTimer);
        top.add(back);
        top.add(lblActive);
        top.add(new JLabel());
        top.add(new JLabel());
        top.add(new JLabel());
        add(top, BorderLayout.NORTH);

        // ---------- Board containers ----------
        lblP1.setHorizontalAlignment(SwingConstants.CENTER);
        lblP2.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel left  = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());
        left.add(lblP1, BorderLayout.NORTH);
        right.add(lblP2, BorderLayout.NORTH);

        board1.setBorder(BorderFactory.createLineBorder(new Color(0,120,215), 2));
        board2.setBorder(BorderFactory.createLineBorder(new Color(46,139,87), 2));
        board1.setBackground(new Color(225,240,255));
        board2.setBackground(new Color(229,244,234));

        left.add(board1, BorderLayout.CENTER);
        right.add(board2, BorderLayout.CENTER);

        JPanel both = new JPanel(new GridLayout(1,2,8,8));
        both.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        both.add(left);
        both.add(right);
        add(both, BorderLayout.CENTER);

        buildBoards();
        refreshAll();

        timer = new Timer(1000, e -> lblTimer.setText("Time: " + ctrl.getElapsedSeconds() + "s"));
        timer.start();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ============================================================
    // BUILD BOARDS
    // ============================================================

    private void buildBoards() {
        int R = ctrl.rows(), C = ctrl.cols();
        btn1 = new JButton[R][C];
        btn2 = new JButton[R][C];

        board1.setLayout(new GridLayout(R,C,1,1));
        board2.setLayout(new GridLayout(R,C,1,1));
        board1.removeAll();
        board2.removeAll();

        for (int r=0;r<R;r++)
            for (int c=0;c<C;c++) {

                JButton a = new JButton("·");
                JButton b = new JButton("·");

                a.setMargin(new Insets(0,0,0,0));
                b.setMargin(new Insets(0,0,0,0));
                a.setFont(a.getFont().deriveFont(Font.BOLD,14f));
                b.setFont(b.getFont().deriveFont(Font.BOLD,14f));
                a.setOpaque(true);
                b.setOpaque(true);
                a.setBackground(new Color(210,230,250));
                b.setBackground(new Color(212,238,219));

                final int rr = r, cc = c;

                // -------------------------------------------
                // PLAYER 1 BOARD CLICK
                // -------------------------------------------
                a.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!ctrl.isPlayer1Active()) return;  // רק השחקן הפעיל
                        final int playerIdx = 0;

                        // תא שאלה/הפתעה שכבר טופל
                        if (ctrl.isQuestionUsed(0, rr, cc) || ctrl.isSurpriseUsed(0, rr, cc)) {
                            JOptionPane.showMessageDialog(
                                    GameViewTwoBoards.this,
                                    "תא זה כבר נבחר בעבר!",
                                    "תא משומש",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
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
                                if (msg != null)
                                    JOptionPane.showMessageDialog(GameViewTwoBoards.this, msg);
                            }
                        }

                        refreshAll();
                        endCheck();
                    }
                });

                // -------------------------------------------
                // PLAYER 2 BOARD CLICK
                // -------------------------------------------
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (ctrl.isPlayer1Active()) return;   // רק השחקן הפעיל
                        final int playerIdx = 1;

                        if (ctrl.isQuestionUsed(1, rr, cc) || ctrl.isSurpriseUsed(1, rr, cc)) {
                            JOptionPane.showMessageDialog(
                                    GameViewTwoBoards.this,
                                    "תא זה כבר נבחר בעבר!",
                                    "תא משומש",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
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
                                if (msg != null)
                                    JOptionPane.showMessageDialog(GameViewTwoBoards.this, msg);
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

    // ============================================================
    // REFRESH ALL
    // ============================================================

    private void refreshAll(){
        boolean p1Active = ctrl.isPlayer1Active();

        // תוויות השחקנים + המציין "ACTIVE BOARD"
        lblP1.setText(ctrl.getP1() + (p1Active ? "  - ACTIVE BOARD" : ""));
        lblP2.setText(ctrl.getP2() + (!p1Active ? "  - ACTIVE BOARD" : ""));

        lblLives.setText("Lives: " + ctrl.getLives());
        lblPoints.setText("Points: " + ctrl.getPoints());
        lblActive.setText("Active: " + (p1Active ? ctrl.getP1() : ctrl.getP2()));
        lblTimer.setText("Time: " + ctrl.getElapsedSeconds() + "s");

        String[][] g1 = ctrl.symbolsOfBoard(0);
        String[][] g2 = ctrl.symbolsOfBoard(1);

        // ----- Board 1 -----
        for (int r=0;r<g1.length;r++)
            for(int c=0;c<g1[0].length;c++){
                JButton btn = btn1[r][c];
                btn.setText(g1[r][c]);

                // צבע בסיס
                btn.setBackground(new Color(210,230,250));
                btn.setForeground(Color.BLACK);

                // תאים משומשים (שאלה/הפתעה שכבר הופעלה)
                if (ctrl.isQuestionUsed(0,r,c) || ctrl.isSurpriseUsed(0,r,c)) {
                    btn.setBackground(new Color(100,100,100));
                    btn.setForeground(Color.WHITE);
                }
            }

        // ----- Board 2 -----
        for (int r=0;r<g2.length;r++)
            for(int c=0;c<g2[0].length;c++){
                JButton btn = btn2[r][c];
                btn.setText(g2[r][c]);

                btn.setBackground(new Color(212,238,219));
                btn.setForeground(Color.BLACK);

                if (ctrl.isQuestionUsed(1,r,c) || ctrl.isSurpriseUsed(1,r,c)) {
                    btn.setBackground(new Color(90,90,90));
                    btn.setForeground(Color.WHITE);
                }
            }

        // הפעלה/כיבוי של הלוחות (כבר היה אצלך)
        setPanelEnabled(board1, p1Active);
        setPanelEnabled(board2, !p1Active);

        repaint();
    }

    private static void setPanelEnabled(Container p, boolean enabled){
        for (Component c : p.getComponents()){
            c.setEnabled(enabled);
            if (c instanceof Container cc)
                setPanelEnabled(cc, enabled);
        }
    }

    private void endCheck(){
        // אם המשחק עדיין לא נגמר – אין מה לעשות
        if (!ctrl.isFinished()) {
            return;
        }

        // אם כבר התחלנו רצף סיום – לא להפעיל שוב
        if (endSequenceStarted) {
            return;
        }
        endSequenceStarted = true;

        // עוצרים את הטיימר של השעון
        if (timer != null) {
            timer.stop();
        }

        // מעדכנים את המסך לפי מצב הלוחות אחרי finishAndClose (כל התאים חשופים)
        refreshAll();

        // טיימר חד-פעמי שאחרי 10 שניות יסגור את המסך ויפתח את EndView
        Timer delay = new Timer(10_000, e -> {
            // סוגרים את חלון המשחק
            dispose();

            // לוקחים את רשומת המשחק מה-Controller
            SysData.GameRecord rec = ctrl.getLastRecord();
            if (app != null && rec != null) {
                app.openEndScreen(rec);
            }
        });
        delay.setRepeats(false);
        delay.start();
    }


    // ============================================================
    // QUESTION UI
    // ============================================================

    @Override
    public int ask(QuestionDTO q){
        Object choice = JOptionPane.showInputDialog(
            this,
            q.text(),
            "Question ("+q.levelLabel()+")",
            JOptionPane.QUESTION_MESSAGE,
            null,
            new Object[]{
                    "1) "+q.options().get(0),
                    "2) "+q.options().get(1),
                    "3) "+q.options().get(2),
                    "4) "+q.options().get(3)},
            null
        );
        if (choice == null) return -1;
        return choice.toString().charAt(0)-'1';
    }

    public void showSelf(){
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
