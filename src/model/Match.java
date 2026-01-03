package model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a single match between two players
 * Holds the boards, scores, lives, timing information, and difficulty
 * Observer pattern (Subject):
 * - Match is the Subject
 * - Views register as MatchListener and receive MatchSnapshot updates
 */
public class Match {
    /** First player */
    private final Player p1;

    /** Second player */
    private final Player p2;

    /** Board for player 1 */
    private final Board b1;

    /** Board for player 2 */
    private final Board b2;

    /** Difficulty level of this match */
    private final DifficultyLevel level;

    /** Remaining lives (shared between players) */
    private int lives;

    /** Current points (shared between players) */
    private int points;

    /** Start time of the match in milliseconds */
    private final long startTimeMs;

    /** Index of active player: 0 = p1, 1 = p2 */
    private int active = 0;

    /** Flag indicating that the match ended (by solving or losing all lives) */
    private boolean finished = false;

    // ========= Lives & cost configuration =========

    /** Maximum number of lives during the match */
    private static final int MAX_LIVES = 10;

    // ========= Observer infrastructure =========

    private final List<MatchListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(MatchListener l){
        if (l != null) listeners.add(l);
    }

    public void removeListener(MatchListener l){
        listeners.remove(l);
    }

    private void notifyListeners(){
        MatchSnapshot s = snapshot();
        for (MatchListener l : listeners){
            l.onMatchChanged(s);
        }
    }

    /**
     * Build a snapshot for observers
     * Uses current cell.symbol() rendering to keep UI decoupled
     */
    public MatchSnapshot snapshot(){
        return new MatchSnapshot(
                p1.name(),
                p2.name(),
                level,
                lives,
                points,
                active,
                elapsedSeconds(),
                isFinished(),
                symbolsOfBoard(b1),
                symbolsOfBoard(b2)
        );
    }

    private String[][] symbolsOfBoard(Board b){
        String[][] g = new String[b.rows()][b.cols()];
        for (int r = 0; r < b.rows(); r++){
            for (int c = 0; c < b.cols(); c++){
                String s = b.cell(r,c).symbol();
                g[r][c] = (s == null || s.isEmpty()) ? "·" : s;
            }
        }
        return g;
    }

    /**
     * Creates a new match between two players at the specified difficulty
     * Initializes the boards and starting lives based on DifficultyConfig
     */
    public Match(Player p1, Player p2, DifficultyLevel level){
        this.p1 = p1;
        this.p2 = p2;
        this.level = level;
        this.b1 = new Board(level);
        this.b2 = new Board(level);

        this.lives = DifficultyConfig.getStartingLives(level);
        this.points = 0;
        this.startTimeMs = System.currentTimeMillis();
    }

    public Player player1(){ return p1; }
    public Player player2(){ return p2; }

    public Board board1(){ return b1; }
    public Board board2(){ return b2; }

    /**
     * return the board belonging to the currently active player
     */
    public Board boardOfActive(){ return active == 0 ? b1 : b2; }

    /**
     * return index of the active player (0 or 1)
     */
    public int activeIndex(){ return active; }

    /**
     * Switches the active player
     */
    public void endTurn(){
        active = 1 - active;
        notifyListeners();
    }

    public DifficultyLevel level(){ return level; }

    public int lives(){ return lives; }
    public int points(){ return points; }

    // ---------- Lives / points logic ----------

    private int lifeValue(){
        return switch (level){
            case EASY   -> 5;   
            case MEDIUM -> 8;    
            case HARD   -> 12;
        };
    }

    /**
     * Adds or removes lives
     * Rules:
     * - When adding lives and exceeding MAX_LIVES, the overflow is converted into points
     *   according to the current difficulty level
     * - When removing lives, the value never goes below 0
     */
    public void addLives(int delta){
        if (delta == 0) return;

        // Removing lives
        if (delta < 0){
            lives = Math.max(0, lives + delta);
            notifyListeners();
            return;
        }

        // delta > 0 -> adding lives
        lives += delta;

        // If lives exceed the maximum, convert the overflow into points
        if (lives > MAX_LIVES){
            int overflow = lives - MAX_LIVES;
            lives = MAX_LIVES;

            int lv = lifeValue();
            points += overflow * lv;
        }

        notifyListeners();
    }

    /**
     * Converts all remaining lives into points at the end of the game
     */
    public void convertLivesToPoints(){
        if (lives <= 0) return;
        int lv = lifeValue();
        points += lives * lv;
        lives = 0;
        notifyListeners();
    }


    /**
     * Adds (or subtracts) points
     */
    public void addPoints(int d){
        points += d;
        notifyListeners();
    }

    /**
     * return true if the match is finished either by flag or by running out of lives
     */
    public boolean isFinished(){
        return finished || lives == 0;
    }

    /**
     * return elapsed time in seconds since the match started
     */
    public long elapsedSeconds(){
        return (System.currentTimeMillis() - startTimeMs) / 1000;
    }

    // ========= Finish conditions =========

    /**
     * Recomputes whether the match is finished
     * The match ends if:
     * - lives reach zero, or
     * - on at least one board:
     *      • every non-mine cell is revealed, OR
     *      • all mines are flagged, OR
     *      • all mines are revealed
     */
    public void checkFinish(){
        boolean before = this.finished;

        // 1. No lives remaining
        if (lives == 0){
            finished = true;
        } else {
            // 2. All mines on board 1 or board 2 are handled:
            //    each mine is either flagged or revealed
            if (allMinesHandled(b1) || allMinesHandled(b2)){
                finished = true;
            }
        }

        // If the finished state has changed, notify listeners
        // Even if finished is already true, an update may still be required
        if (before != this.finished || this.finished){
            notifyListeners();
        }
    }

    /**
     * Checks whether a board is fully solved:
     * all non-mine cells have been revealed
     */
    private boolean boardSolved(Board b){
        for(int r = 0; r < b.rows(); r++){
            for(int c = 0; c < b.cols(); c++){
                Cell cell = b.cell(r,c);
                if(!cell.isRevealed() && !(cell instanceof MineCell))
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all mine cells on the board
     * are either flagged or revealed
     */
    private boolean allMinesHandled(Board b){
        for (int r = 0; r < b.rows(); r++){
            for (int c = 0; c < b.cols(); c++){
                Cell cell = b.cell(r,c);
                if (cell instanceof MineCell){
                    // A mine that is neither revealed nor flagged is not yet handled
                    if (!cell.isRevealed() && !cell.isFlagged()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Creates a GameRecord snapshot from this match
     * for storing the result in the game history
     * @param won indicates whether the players won the game
     */
    public SysData.GameRecord toRecord(boolean won){
        return new SysData.GameRecord(
                p1.name(), p2.name(), level, lives, points, won, elapsedSeconds()
        );
    }
}

