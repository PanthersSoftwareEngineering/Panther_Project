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

/**
 * GameViewTwoBoards is the main match screen for a 2-player game
 * Key responsibilities:
 * - Display two boards side-by-side (Player 1 left, Player 2 right)
 * - Show HUD information (names, lives, points, timer, active player, difficulty)
 * - Forward user actions (left/right click) to MatchController
 * - React to model updates using the Observer pattern (MatchListener -> MatchSnapshot)
 * - This view does not compute game logic. It delegates actions to MatchController
 * - UI refresh is driven by snapshots published by the controller/model
 */
public class GameViewTwoBoards extends BaseGameFrame implements QuestionUI, MatchListener {

    /** Controller that holds match logic (reveal/flag/interactions/scoring/turns) */
    private final MatchController ctrl;

    /** App-level controller used for navigation between screens (e.g., end screen, main menu)*/
    private final AppController app;

    // ========================= HUD Labels =========================
    /** Player names shown above each board. */
    private final JLabel lblP1 = new JLabel();
    private final JLabel lblP2 = new JLabel();

    /** HUD values: lives, points, time, active player, and difficulty */
    private final JLabel lblLives  = new JLabel();
    private final JLabel lblPoints = new JLabel();
    private final JLabel lblTimer  = new JLabel();
    private final JLabel lblActive = new JLabel();
    private final JLabel lblDifficulty = new JLabel();

    // ========================= ACTIVE chips =========================
    /** Small badge that indicates which board/player is currently active (turn-based) */
    private final JLabel chipP1Active = new JLabel("ACTIVE", SwingConstants.CENTER);
    private final JLabel chipP2Active = new JLabel("ACTIVE", SwingConstants.CENTER);

    // ========================= Board containers =========================
    /** Board panels (each contains a grid of CellButton components) */
    private final JPanel board1 = new JPanel();
    private final JPanel board2 = new JPanel();

    /** Buttons grids: btn1 for Player 1 board, btn2 for Player 2 board */
    private CellButton[][] btn1, btn2;

    // ========================= Timers and state =========================
    /** Swing timer used for updating the "Time" label every second */
    private Timer timer;

    /** Prevents running the end-sequence twice when match finished snapshot arrives multiple times */
    private boolean endSequenceStarted = false;

    /**
     * Holds the latest snapshot for possible timer syncing / debugging
     * volatile ensures visibility across Swing timer thread and EDT
     */
    private volatile MatchSnapshot lastSnapshot = null;

    /**
     * Constructs the view, registers it as an observer, and builds the full UI
     */
    public GameViewTwoBoards(MatchController ctrl, AppController app) {
        super(app, "Minesweeper - Match");
        this.ctrl = ctrl;
        this.app  = app;

        // The controller will use this QuestionUI when it needs to ask the player a question
        ctrl.setQuestionUI(this);

        // Observer pattern: this view listens to match changes via snapshots
        ctrl.addMatchListener(this);

        // ========================= Background =========================
        BackgroundPanel bg = new BackgroundPanel(GameAssets.MATCH_BACKGROUND);
        bg.setLayout(new BorderLayout(8, 8));
        setContentPane(bg);

        // Enables toast messages to appear above all UI components
        installToastLayer();

        // ========================= HUD (top area) =========================
        JPanel hud = UIStyles.translucentPanel(new BorderLayout(), UIStyles.HUD_PANEL_BG);
        hud.setBorder(UIStyles.pad(12, 16, 12, 16));

        // Difficulty label on top-left
        UIStyles.styleTitle(lblDifficulty);
        lblDifficulty.setHorizontalAlignment(SwingConstants.LEFT);
        lblDifficulty.setText("Difficulty: " + ctrl.getDiff());

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        row1.add(lblDifficulty, BorderLayout.WEST);

        // Style HUD labels consistently (font, color, etc.)
        UIStyles.styleHudLabel(lblLives);
        UIStyles.styleHudLabel(lblPoints);
        UIStyles.styleHudLabel(lblTimer);
        UIStyles.styleHudLabel(lblActive);

        // Back button returns to main menu and unregisters listener to prevent memory leaks
        BaseGameFrame.RoundedButton back =
                new BaseGameFrame.RoundedButton("Back to Main", 260, 64, 22);

        back.addActionListener(e -> {
            // Stop the timer so it won't continue firing after closing the window
            if (timer != null) timer.stop();

            // Unregister as observer to avoid stale listeners / leaks
            ctrl.removeMatchListener(this);

            dispose();
            if (app != null) app.showMainMenu();
        });

        // Second HUD row: lives, points, timer, active player, and the back button
        JPanel row2 = new JPanel(new GridLayout(1, 5, 14, 0));
        row2.setOpaque(false);
        row2.add(lblLives);
        row2.add(lblPoints);
        row2.add(lblTimer);
        row2.add(lblActive);
        row2.add(back);

        // Stack HUD rows vertically
        JPanel hudInner = new JPanel();
        hudInner.setOpaque(false);
        hudInner.setLayout(new BoxLayout(hudInner, BoxLayout.Y_AXIS));
        hudInner.add(row1);
        hudInner.add(Box.createVerticalStrut(8));
        hudInner.add(row2);

        hud.add(hudInner, BorderLayout.CENTER);

        // Wrapper for consistent spacing from the window edges
        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.setOpaque(false);
        topWrap.setBorder(UIStyles.pad(8, 8, 0, 8));
        topWrap.add(hud, BorderLayout.CENTER);

        bg.add(topWrap, BorderLayout.NORTH);

        // ========================= Board headers (names + ACTIVE chip) =========================
        lblP1.setHorizontalAlignment(SwingConstants.CENTER);
        lblP2.setHorizontalAlignment(SwingConstants.CENTER);
        UIStyles.styleTitle(lblP1);
        UIStyles.styleTitle(lblP2);

        JPanel left  = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());
        left.setOpaque(false);
        right.setOpaque(false);

        // ACTIVE chips styling (background color updated in refreshFromSnapshot)
        chipP1Active.setOpaque(true);
        chipP1Active.setForeground(UIStyles.CHIP_TEXT);
        chipP1Active.setFont(UIStyles.HUD_FONT_SMALL);
        chipP1Active.setBorder(UIStyles.pad(4, 10, 4, 10));

        chipP2Active.setOpaque(true);
        chipP2Active.setForeground(UIStyles.CHIP_TEXT);
        chipP2Active.setFont(UIStyles.HUD_FONT_SMALL);
        chipP2Active.setBorder(UIStyles.pad(4, 10, 4, 10));

        // Header panels: player name + ACTIVE badge next to it
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

        // Borders used to visually separate each board
        board1.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
        board2.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2));
        board1.setOpaque(false);
        board2.setOpaque(false);

        left.add(board1, BorderLayout.CENTER);
        right.add(board2, BorderLayout.CENTER);

        // Place boards side-by-side with padding/gap
        JPanel both = new JPanel(new GridLayout(1, 2, 8, 8));
        both.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        both.setOpaque(false);
        both.add(left);
        both.add(right);

        bg.add(both, BorderLayout.CENTER);

        // Build the grid of buttons for each board once (listeners delegate to controller)
        buildBoards();

        // ========================= Timer =========================
        // Only updates the clock + difficulty label (UI refresh of board comes from snapshots)
        timer = new Timer(1000, e -> {
            lblTimer.setText("Time: " + UIStyles.formatTimeMMSS(ctrl.getElapsedSeconds()));
            lblDifficulty.setText("Difficulty: " + ctrl.getDiff());
        });
        timer.start();

        // Make the match view fullscreen and fixed-size
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // ========================= Observer callback =========================

    /**
     * Called by the controller/model whenever the match state changes.
     * We update the UI based on the snapshot, always on the Swing EDT.
     */
    @Override
    public void onMatchChanged(MatchSnapshot s) {
        this.lastSnapshot = s;

        // Ensure UI changes happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            refreshFromSnapshot(s);
            endCheck(s);
        });
    }

    // ========================= Board building =========================

    /**
     * Creates the button grids for both players and attaches mouse listeners.
     * Each listener only calls controller methods (no game logic in the view).
     */
    private void buildBoards() {
        int R = ctrl.rows(), C = ctrl.cols();
        btn1 = new CellButton[R][C];
        btn2 = new CellButton[R][C];

        // Grid layout for the boards
        board1.setLayout(new GridLayout(R, C, 6, 6));
        board2.setLayout(new GridLayout(R, C, 6, 6));
        board1.removeAll();
        board2.removeAll();

        Dimension cellSize = new Dimension(44, 44);

        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {

                // Default appearance: hidden cells show a dot placeholder
                CellButton a = new CellButton("·", CellStyle.P1_BASE);
                CellButton b = new CellButton("·", CellStyle.P2_BASE);
                a.setPreferredSize(cellSize);
                b.setPreferredSize(cellSize);

                // Capture coordinates for the listener closure
                final int rr = r, cc = c;

                // Player 1 board click: only works when Player 1 is active
                a.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!ctrl.isPlayer1Active()) return;
                        handleClick(0, rr, cc, e);
                    }
                });

                // Player 2 board click: only works when Player 2 is active
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (ctrl.isPlayer1Active()) return;
                        handleClick(1, rr, cc, e);
                    }
                });

                // Store references for later UI updates from snapshots
                btn1[r][c] = a;
                btn2[r][c] = b;

                board1.add(a);
                board2.add(b);
            }
        }

        // Rebuild UI layout after adding components
        board1.revalidate();
        board2.revalidate();
        board1.repaint();
        board2.repaint();
    }

    // ========================= Click handling =========================

    /**
     * Handles a click on a specific cell and delegates to the controller.
     * This method enforces UI-level rules (like "don't reveal flagged cell").
     *
     * @param playerIdx which board was clicked (0 = P1, 1 = P2)
     * @param r row index
     * @param c col index
     * @param e original mouse event (for left/right click detection)
     */
    private void handleClick(int playerIdx, int r, int c, MouseEvent e) {

        // Block interaction for cells already consumed (question/surprise already used)
        if (ctrl.isQuestionUsed(playerIdx, r, c) || ctrl.isSurpriseUsed(playerIdx, r, c)) {
            Toast.show(this, "Cell Already Chosen!");
            return;
        }

        // Right click toggles a flag (controller handles scoring + end turn)
        if (SwingUtilities.isRightMouseButton(e)) {
            ctrl.toggleFlag(playerIdx, r, c);
            return; // Snapshot will arrive and refresh the UI
        }

        // UX rule: left click should not auto-remove a flag
        // Player must explicitly right-click to remove the flag first
        if (ctrl.isFlagged(playerIdx, r, c)) {
            Toast.show(this, "Remove flag first (Right-click) to reveal.");
            return;
        }

        // Attempt to interact with pending Question/Surprise cell (if this cell is pending)
        if (ctrl.tryInteract(playerIdx, r, c)) {
            showLastInteractionToast();
            return;
        }

        // Normal reveal (only if cell is still hidden)
        if (!ctrl.isRevealed(playerIdx, r, c)) {
            ctrl.reveal(r, c);
        }

        // If interaction produced a message (surprise/question)
        showLastInteractionToast();
    }

    /**
     * Shows the latest interaction message from the controller, including:
     * - activation cost
     * - effect points/lives
     * - net change
     * This is a UI-only feature: controller stores the last interaction summary
     */
    private void showLastInteractionToast() {
        String msg = ctrl.consumeLastInteractionMessage();
        if (msg == null) return;

        String details =
                " cost:" + fmtDelta(-ctrl.getLastActivationCost(), "pts") +
                " effect:" + fmtDelta(ctrl.getLastEffectPoints(), "pts") + "," + fmtDelta(ctrl.getLastEffectLives(), "❤") +
                " net:" + fmtDelta(ctrl.getLastNetPoints(), "pts") + "," + fmtDelta(ctrl.getLastNetLives(), "❤");

        Toast.show(this, msg + "  " + details);
    }

    /** Formats deltas consistently for toast messages (+ / - / 0) */
    private String fmtDelta(int v, String unit) {
        if (v > 0) return "+" + v + " " + unit;
        if (v < 0) return v + " " + unit;
        return "0 " + unit;
    }

    // ========================= Snapshot -> UI refresh =========================

    /**
     * Updates all UI components based on the snapshot.
     * This keeps the view fully synchronized with the model state
     */
    private void refreshFromSnapshot(MatchSnapshot s) {
        boolean p1Active = (s.activeIndex() == 0);
        boolean finished = s.finished();

        // Player names in headers
        lblP1.setText(s.p1());
        lblP2.setText(s.p2());

        // HUD values
        lblLives.setText("Lives: " + s.lives());
        lblPoints.setText("Points: " + s.points());
        lblTimer.setText("Time: " + UIStyles.formatTimeMMSS(ctrl.getElapsedSeconds()));
        lblActive.setText("Active: " + (p1Active ? s.p1() : s.p2()));
        lblDifficulty.setText("Difficulty: " + s.level().name());

        // ACTIVE chip visibility and color
        // (Optional but recommended) hide chips when finished
        chipP1Active.setVisible(!finished && p1Active);
        chipP2Active.setVisible(!finished && !p1Active);
        chipP1Active.setBackground(UIStyles.CHIP_BG_ACTIVE_P1);
        chipP2Active.setBackground(UIStyles.CHIP_BG_ACTIVE_P2);

        // Board symbols from the snapshot (already computed by model/controller)
        String[][] g1 = s.boardP1();
        String[][] g2 = s.boardP2();

        // Refresh Player 1 board buttons
        for (int r = 0; r < g1.length; r++) {
            for (int c = 0; c < g1[0].length; c++) {
                CellButton btn = btn1[r][c];
                String sym = g1[r][c];
                btn.setText(sym);

                // Color mapping is UI-only (symbol -> style)
                btn.setBaseColor(CellStyle.colorForSymbol(sym, 0));
                btn.setForeground(CellStyle.textColorForSymbol(sym));

                // Visually mark used question/surprise cells
                if (ctrl.isQuestionUsed(0, r, c) || ctrl.isSurpriseUsed(0, r, c)) {
                    btn.setBaseColor(CellStyle.USED);
                    btn.setForeground(Color.WHITE);
                }
            }
        }

        // Refresh Player 2 board buttons
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

        
        // During play: disable the inactive board (turn-based)
        // After finish: enable BOTH boards so revealed colors are not greyed out
        setPanelEnabled(board1, finished || p1Active);
        setPanelEnabled(board2, finished || !p1Active);

        repaint();
    }


    /**
     * Recursively enables/disables an entire container and all its children
     * Used to lock the inactive player's board during the other player's turn
     */
    private static void setPanelEnabled(Container p, boolean enabled) {
        for (Component c : p.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container cc) setPanelEnabled(cc, enabled);
        }
    }

    // ========================= End-of-game handling =========================

    /**
     * Checks if the match finished
     * If finished: stops timer, waits briefly (2s), then opens EndScreen
     */
    private void endCheck(MatchSnapshot s) {
        if (!s.finished()) return;
        if (endSequenceStarted) return; // guard against duplicate triggers
        endSequenceStarted = true;

        if (timer != null) timer.stop();

        // Small delay so the player can see the final board state before switching screens
        Timer delay = new Timer(2_000, e -> {
            ctrl.removeMatchListener(this);
            dispose();

            // Last record is produced by controller when the match ends
            SysData.GameRecord rec = ctrl.getLastRecord();
            if (app != null && rec != null) {
                app.openEndScreen(rec);
            }
        });
        delay.setRepeats(false);
        delay.start();
    }

    // ========================= QuestionUI implementation =========================

    /**
     * Called by controller when a Question cell is activated.
     * Shows a dialog and returns the chosen answer index
     * Return contract:
     * - 0..3 = selected option index
     * - (implementation-dependent) negative can mean "cancel"
     */
    @Override
    public int ask(QuestionDTO q) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);

        // Correct index is provided by controller so the dialog can mark the correct answer if needed
        int correct = ctrl.getLastQuestionCorrectIndex();

        QuestionDialog dialog = new QuestionDialog(owner, q, correct);
        return dialog.showDialog();
    }

    /**
     * Optional confirmation step before activating a Question/Surprise cell
     * This allows the user to avoid paying the activation cost
     */
    @Override
    public boolean confirmActivation(String kindLabel, int costPoints) {

        String msg =
                "Do you want to activate this " + kindLabel + " cell?\n" +
                "Activation cost: " + costPoints + " points\n" +
                "After activation, you may gain/lose points and hearts.";

        int res = StyledConfirmDialog.show(this, msg, JOptionPane.YES_NO_OPTION);

        // StyledConfirmDialog returns OK/CANCEL
        return res == JOptionPane.OK_OPTION;
    }

    @Override
    public void showSelf() {
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
