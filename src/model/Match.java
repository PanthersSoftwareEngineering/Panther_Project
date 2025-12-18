package model;

/**
 * Represents a single match between two players.
 * Holds the boards, scores, lives, timing information, and difficulty.
 */
public class Match {
    /** First player. */
    private final Player p1;

    /** Second player. */
    private final Player p2;

    /** Board for player 1. */
    private final Board b1;

    /** Board for player 2. */
    private final Board b2;

    /** Difficulty level of this match. */
    private final DifficultyLevel level;

    /** Remaining lives (shared between players). */
    private int lives;

    /** Current points (shared between players). */
    private int points;

    /** Start time of the match in milliseconds. */
    private final long startTimeMs;

    /** Index of active player: 0 = p1, 1 = p2. */
    private int active = 0;

    /** Flag indicating that the match ended (by solving or losing all lives). */
    private boolean finished = false;

    // ========= Lives & cost configuration =========

    /** Maximum number of lives during the match. */
    private static final int MAX_LIVES = 10;

    /**
     * Creates a new match between two players at the specified difficulty.
     * Initializes the boards and starting lives based on DifficultyConfig.
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
     * @return the board belonging to the currently active player.
     */
    public Board boardOfActive(){ return active == 0 ? b1 : b2; }

    /**
     * @return index of the active player (0 or 1).
     */
    public int activeIndex(){ return active; }

    /**
     * Switches the active player.
     */
    public void endTurn(){ active = 1 - active; }

    public DifficultyLevel level(){ return level; }

    public int lives(){ return lives; }
    public int points(){ return points; }

    // ---------- Lives / points logic ----------

    /** כמה נקודות שווה חיים אחד לפי רמת קושי */
    private int lifeValue(){
        return switch (level){
            case EASY   -> 5;   // עלות "חיים" במשחק קל
            case MEDIUM -> 8;   // בינוני
            case HARD   -> 12;  // קשה
        };
    }

    /**
     * הוספת/הפחתת חיים:
     * - אם מוסיפים חיים ועוברים את 10 → ההפרש מומר לנקודות לפי רמת הקושי
     * - אם מורידים חיים → לא יורדים מתחת ל-0
     */
    public void addLives(int delta){
        if (delta == 0) return;

        // הורדת חיים
        if (delta < 0){
            lives = Math.max(0, lives + delta);
            return;
        }

        // כאן delta > 0 → מוסיפים חיים
        lives += delta;

        // אם עברנו את התקרה, ההפרש הופך לנקודות
        if (lives > MAX_LIVES){
            int overflow = lives - MAX_LIVES;
            lives = MAX_LIVES;

            int lv = lifeValue();
            points += overflow * lv;
        }
    }

    /** המרת כל החיים שנותרו לנקודות בסיום המשחק */
    public void convertLivesToPoints(){
        if (lives <= 0) return;
        int lv = lifeValue();
        points += lives * lv;
        lives = 0;
    }

    /**
     * Adds (or subtracts) points.
     */
    public void addPoints(int d){
        points += d;
    }

    /**
     * @return true if the match is finished either by flag or by running out of lives.
     */
    public boolean isFinished(){
        return finished || lives == 0;
    }

    /**
     * @return elapsed time in seconds since the match started.
     */
    public long elapsedSeconds(){
        return (System.currentTimeMillis() - startTimeMs) / 1000;
    }

    // ========= Finish conditions =========

    /**
     * Recomputes whether the match is finished.
     * The match ends if:
     * - lives reach zero, or
     * - on at least one board:
     *      • every non-mine cell is revealed (פתרון רגיל), OR
     *      • all mines are flagged, OR
     *      • all mines are revealed.
     */
    public void checkFinish(){
        // 1. נגמרו חיים
        if (lives == 0){
            finished = true;
            return;
        }

        // 2. אחד הלוחות נפתר (כל התאים הבטוחים נחשפו)
        /**if (boardSolved(b1) || boardSolved(b2)){
            finished = true;
            return;
        }*/

        // 3. בכל מוקש בלוח 1 או 2: או דגל או חשוף
        if (allMinesHandled(b1) || allMinesHandled(b2)){
            finished = true;
        }
    }


    /**
     * Checks whether a board is solved: every non-mine cell is revealed.
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
     * מחזירה true אם כל תאי המוקש בלוח 
     * או מסומנים בדגל או חשופים.
     */
    private boolean allMinesHandled(Board b){
        for (int r = 0; r < b.rows(); r++){
            for (int c = 0; c < b.cols(); c++){
                Cell cell = b.cell(r,c);
                if (cell instanceof MineCell){
                    // אם המוקש לא חשוף וגם לא מסומן בדגל → עוד לא "טופל"
                    if (!cell.isRevealed() && !cell.isFlagged()){
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Creates a GameRecord snapshot from this match for storing in history.
     *
     * @param won indicates whether the players won the game
     */
    public SysData.GameRecord toRecord(boolean won){
        return new SysData.GameRecord(
                p1.name(), p2.name(), level, lives, points, won, elapsedSeconds()
        );
    }
}
