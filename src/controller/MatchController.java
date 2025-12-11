package controller;

import java.util.*;

import model.*;
import view.QuestionDTO;
import view.QuestionUI;

/**
 * Main controller for a single match (Singleton).
 * Coordinates between the model (Match, Board, SysData) and the game view.
 * Contains all game logic such as revealing cells, handling questions,
 * surprise cells, scoring, and turn switching.
 */
public class MatchController {

    // ---------- Singleton ----------
    private static MatchController INSTANCE;

    public static synchronized MatchController getInstance(){
        if (INSTANCE == null) INSTANCE = new MatchController();
        return INSTANCE;
    }

    private MatchController(){}

    // ---------- Current session state ----------
    private Match       match;
    private SysData     sys;
    private AppController app;
    private QuestionUI  questionUI;

    /** Random generator for surprises and 50/50 cases in question table. */
    private final Random rnd = new Random();

    // Directions of 8 neighbors (for flood reveal).
    private static final int[] DR = {-1,-1,-1,0,0,1,1,1};
    private static final int[] DC = {-1, 0, 1,-1,1,-1,0,1};

    /** Key for pending interactions (question / surprise) per player. */
    private static final class Key {
        final int r,c;
        final boolean isQuestion;
        Key(int r,int c,boolean q){ this.r=r; this.c=c; this.isQuestion=q; }
        @Override public boolean equals(Object o){
            if(this==o) return true;
            if(!(o instanceof Key k)) return false;
            return r==k.r && c==k.c && isQuestion==k.isQuestion;
        }
        @Override public int hashCode(){ return Objects.hash(r,c,isQuestion); }
    }

    private final Set<Key> pendingP1 = new HashSet<>();
    private final Set<Key> pendingP2 = new HashSet<>();

    private String lastSurpriseMessage = null;

    // ---------- Question & Surprise effect holders ----------

    /** אפקט של שאלה: נקודות + חיים + בונוסים מיוחדים */
    private static final class QuestionEffect {
        final int pointsDelta;
        final int livesDelta;
        final boolean revealMineBonus;   // חשיפת מוקש (ללא ניקוד)
        final boolean revealAreaBonus;   // חשיפת 3x3 (ללא ניקוד)

        QuestionEffect(int p,int l,boolean mine,boolean area){
            this.pointsDelta = p;
            this.livesDelta  = l;
            this.revealMineBonus = mine;
            this.revealAreaBonus = area;
        }
    }

    /** אפקט של Surprise: נקודות + חיים */
    private static final class SurpriseEffect {
        final int pointsDelta;
        final int livesDelta;
        SurpriseEffect(int p,int l){ pointsDelta=p; livesDelta=l; }
    }

    // ---------- init/reset for a new match ----------
    public synchronized void init(Match match, SysData sys, AppController app){
        this.match = match;
        this.sys   = sys;
        this.app   = app;
        this.questionUI = null;
        pendingP1.clear();
        pendingP2.clear();
        lastSurpriseMessage = null;
    }

    public void setQuestionUI(QuestionUI ui){
        this.questionUI = ui;
    }

    // ---------- Read-only accessors ----------
    public boolean isFinished(){ return match != null && match.isFinished(); }
    public String  getP1(){ return match.player1().name(); }
    public String  getP2(){ return match.player2().name(); }
    public String  getDiff(){ return match.level().name(); }
    public int     getLives(){ return match.lives(); }
    public int     getPoints(){ return match.points(); }
    public boolean isPlayer1Active(){ return match.activeIndex() == 0; }
    public long    getElapsedSeconds(){ return match.elapsedSeconds(); }
    public int     rows(){ return match.board1().rows(); }
    public int     cols(){ return match.board1().cols(); }

    public boolean isRevealed(int playerIdx,int row,int col){
        Board b = (playerIdx==0)? match.board1() : match.board2();
        return b.cell(row,col).isRevealed();
    }

    public String[][] symbolsOfBoard(int playerIndex){
        Board b = (playerIndex==0)? match.board1() : match.board2();
        String[][] g = new String[b.rows()][b.cols()];
        for(int r=0;r<b.rows();r++) for(int c=0;c<b.cols();c++){
            String s = b.cell(r,c).symbol();
            g[r][c] = s.isEmpty()? "·" : s;
        }
        return g;
    }

    // ======================== Actions ========================

    public void reveal(int row,int col){
        Board b = match.boardOfActive();
        Cell  cell = b.cell(row,col);
        if (cell.isRevealed()) return;

        int playerIdx = match.activeIndex();

        if (cell instanceof EmptyCell) {
            floodReveal(b, playerIdx, row, col);
        } else {
            cell.reveal();
            applyRevealScoring(cell);

            if (cell instanceof QuestionCell)
                addPending(playerIdx, new Key(row,col,true));
            else if (cell instanceof SurpriseCell)
                addPending(playerIdx, new Key(row,col,false));
        }

        match.checkFinish();
        if (match.isFinished()) {
            finishAndClose();
            return;
        }

        endTurn();
    }

    /**
     * Tries to interact with a pending Question/SURPRISE cell.
     * For QuestionCell – pulls a random Question from SysData (any level),
     * and applies scoring based on game difficulty + question level
     * according to the design table.
     */
    public boolean tryInteract(int playerIdx,int row,int col){
        if (playerIdx != match.activeIndex()) return false;

        Set<Key> pend = pendSet(playerIdx);
        Key qKey = new Key(row,col,true);
        Key sKey = new Key(row,col,false);

        Board b = (playerIdx==0)? match.board1() : match.board2();
        Cell  cell = b.cell(row,col);

        // ----- Question cell -----
        if (pend.contains(qKey) && cell instanceof QuestionCell){

            Question      q = sys.drawRandomQuestion();
            boolean       right;
            QuestionLevel qLevel;

            if (q == null){
                // אין שאלות – מתייחסים כאילו ענה נכון על שאלה קלה
                lastSurpriseMessage = "No questions in bank. Counted as correct.";
                right  = true;
                qLevel = QuestionLevel.EASY;

            } else {
                QuestionDTO dto = new QuestionDTO(
                        q.id(), q.text(), q.options(), q.level().name()
                );

                if (questionUI != null){
                    int choice = questionUI.ask(dto);

                    // cancel (או סגירת חלון) → לא לענות, לא לשנות תור, לא לגבות עלות
                    if (choice < 0){
                        return false;
                    }
                    right  = (choice == q.correctIndex());
                    qLevel = q.level();
                } else {
                    // אין UI – נניח תשובה נכונה
                    right  = true;
                    qLevel = q.level();
                }
            }

            // *** עלות הפעלת שאלה לפי רמת משחק ***
            int cost = activationCost(match.level());
            match.addPoints(-cost);

            // אפקט מלא לפי הטבלה (נקודות + לבבות + בונוסים)
            DifficultyLevel diff = match.level();
            QuestionEffect eff = computeQuestionEffect(diff, qLevel, right);

            match.addPoints(eff.pointsDelta);
            match.addLives (eff.livesDelta);

            if (eff.revealMineBonus){
                revealRandomMineBonus(b, playerIdx);
            }
            if (eff.revealAreaBonus){
                revealRandom3x3Bonus(b, playerIdx);
            }

            // השאלה טופלה – מסירים מה-pending
            pend.remove(qKey);

        // ----- Surprise cell -----
        } else if (pend.contains(sKey) && cell instanceof SurpriseCell sc){
            if (sc.isRevealed() && !sc.wasOperated()){

                sc.operate();

                // *** עלות הפעלת Surprise לפי רמת משחק ***
                int cost = activationCost(match.level());
                match.addPoints(-cost);

                // 50/50 הפתעה טובה / רעה
                boolean good = rnd.nextBoolean();
                SurpriseEffect se = computeSurpriseEffect(match.level(), good);

                match.addPoints(se.pointsDelta);
                match.addLives (se.livesDelta);

                lastSurpriseMessage = good
                        ? "🎁 Good surprise! +" + se.pointsDelta + " pts, +" + se.livesDelta + " ❤"
                        : "🎁 Bad surprise! "  + se.pointsDelta + " pts, "  + se.livesDelta + " ❤";
            }
            pend.remove(sKey);

        } else {
            return false;
        }

        // אחרי אינטראקציה *אמיתית* (לא cancel) – בודקים סוף משחק וסיום תור
        match.checkFinish();
        if (match.isFinished()) {
            finishAndClose();
            return true;
        }

        endTurn();
        return true;
    }


    public void toggleFlag(int playerIndex,int row,int col){
        if (playerIndex != match.activeIndex()) return;

        Board b = (playerIndex==0)? match.board1() : match.board2();
        Cell  cell = b.cell(row,col);
        boolean before = cell.isFlagged();

        cell.toggleFlag();

        if (before != cell.isFlagged())
            applyFlagScoring(cell, cell.isFlagged());

        endTurn();
    }

    // ======================== Flood ========================

    private void floodReveal(Board b,int playerIdx,int sr,int sc){
        int R = b.rows(), C = b.cols();
        boolean[][] seen = new boolean[R][C];
        ArrayDeque<int[]> q = new ArrayDeque<>();

        q.add(new int[]{sr,sc});
        seen[sr][sc] = true;

        while(!q.isEmpty()){
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];
            Cell cell = b.cell(r,c);

            if (cell.isRevealed()) continue;

            cell.reveal();
            applyRevealScoring(cell);

            // אם זו שאלה – מסמנים כ-pending אבל לא עוצרים את ההצפה
            if (cell instanceof QuestionCell){
                addPending(playerIdx,new Key(r,c,true));
            }

            // אם זו תחנת הפתעה – גם מוסיפים כ-pending אבל ממשיכים להתרחב
            if (cell instanceof SurpriseCell){
                addPending(playerIdx,new Key(r,c,false));
            }

            // מספרים לא מרחיבים הלאה (כמו בשולה מוקשים רגיל)
            if (cell instanceof NumberCell) {
                continue;
            }

            // מוקש לעולם לא ניכנס אליו בהצפה
            if (cell instanceof MineCell) {
                continue;
            }

            // מרחיבים לשכנים עבור תאים ריקים + שאלות + הפתעות (אבל לא מוקשים)
            for(int k=0;k<8;k++){
                int nr = r + DR[k], nc = c + DC[k];
                if(nr<0 || nr>=R || nc<0 || nc>=C) continue;
                if(seen[nr][nc]) continue;
                if(b.cell(nr,nc) instanceof MineCell) continue;
                seen[nr][nc] = true;
                q.add(new int[]{nr,nc});
            }
        }
    }


    // ======================== Finish ========================

    private void finishAndClose(){
        revealAllBoards();
        SysData.GameRecord rec = match.toRecord(match.lives() > 0);
        sys.addRecord(rec);
        app.openEndScreen(rec);
    }

    private void revealAllBoards(){
        Board b1 = match.board1();
        Board b2 = match.board2();

        for(int r=0;r<b1.rows();r++) for(int c=0;c<b1.cols();c++){
            Cell x = b1.cell(r,c);
            if(!x.isRevealed()) x.reveal();
        }
        for(int r=0;r<b2.rows();r++) for(int c=0;c<b2.cols();c++){
            Cell x = b2.cell(r,c);
            if(!x.isRevealed()) x.reveal();
        }
    }

    // ======================== Helpers ========================

    public String consumeLastSurpriseMessage(){
        String s = lastSurpriseMessage;
        lastSurpriseMessage = null;
        return s;
    }

    private void endTurn(){
        match.endTurn();
    }

    private Set<Key> pendSet(int idx){
        return idx==0 ? pendingP1 : pendingP2;
    }

    private void addPending(int idx, Key k){
        pendSet(idx).add(k);
    }

    /** Basic scoring for reveal (לא קשור לטבלת השאלות) */
    private void applyRevealScoring(Cell cell){
        switch(cell.type()){
            case MINE -> match.addLives(-1);
            default   -> match.addPoints(+1);
        }
    }

    private void applyFlagScoring(Cell cell, boolean nowFlagged){
        switch(cell.type()){
            case MINE -> match.addPoints(nowFlagged? +1 : -1);
            default   -> match.addPoints(nowFlagged? -3 : +3);
        }
    }

    // ======================== Question & Surprise scoring ========================

    /** עלות הפעלת שאלה / הפתעה לפי רמת קושי */
    private int activationCost(DifficultyLevel diff){
        return switch (diff){
            case EASY   -> 5;
            case MEDIUM -> 8;
            case HARD   -> 12;
        };
    }

    /**
     * מחזירה את כל האפקט של שאלה:
     *  - שינוי נקודות
     *  - שינוי לבבות
     *  - בונוסים של חשיפת מוקש / 3x3 (ללא ניקוד/לב)
     *
     * ממומש אחד-לא-אחד לפי הטבלה.
     */
    private QuestionEffect computeQuestionEffect(DifficultyLevel diff,
                                                 QuestionLevel ql,
                                                 boolean right){
        switch (diff){
            case EASY:
                switch (ql){
                    case EASY:
                        if (right) {
                            return new QuestionEffect(+3, +1, false, false);
                        } else {
                            boolean punish = rnd.nextBoolean();
                            return punish
                                    ? new QuestionEffect(-3, 0, false, false)
                                    : new QuestionEffect(0, 0, false, false);
                        }

                    case MEDIUM:
                        if (right) {
                            return new QuestionEffect(+6, 0, true, false);
                        } else {
                            boolean punish = rnd.nextBoolean();
                            return punish
                                    ? new QuestionEffect(-6, 0, false, false)
                                    : new QuestionEffect(0, 0, false, false);
                        }

                    case HARD:
                        if (right) {
                            return new QuestionEffect(+10, 0, false, true);
                        } else {
                            return new QuestionEffect(-10, 0, false, false);
                        }

                    case MASTER:
                        if (right) {
                            return new QuestionEffect(+15, +1, false, false);
                        } else {
                            return new QuestionEffect(-15, -1, false, false);
                        }
                }
                break;

            case MEDIUM:
                switch (ql){
                    case EASY:
                        if (right) {
                            return new QuestionEffect(+8, +1, false, false);
                        } else {
                            return new QuestionEffect(-8, 0, false, false);
                        }

                    case MEDIUM:
                        if (right) {
                            return new QuestionEffect(+10, +1, false, false);
                        } else {
                            boolean punish = rnd.nextBoolean();
                            if (punish){
                                return new QuestionEffect(-10, -1, false, false);
                            } else {
                                return new QuestionEffect(0, 0, false, false);
                            }
                        }

                    case HARD:
                        if (right) {
                            return new QuestionEffect(+15, +1, false, false);
                        } else {
                            return new QuestionEffect(-15, -1, false, false);
                        }

                    case MASTER:
                        if (right) {
                            return new QuestionEffect(+20, +2, false, false);
                        } else {
                            boolean oneOrTwo = rnd.nextBoolean();
                            return new QuestionEffect(-20, oneOrTwo ? -1 : -2, false, false);
                        }
                }
                break;

            case HARD:
                switch (ql){
                    case EASY:
                        if (right) {
                            return new QuestionEffect(+10, +1, false, false);
                        } else {
                            return new QuestionEffect(-10, -1, false, false);
                        }

                    case MEDIUM:
                        if (right) {
                            boolean oneOrTwo = rnd.nextBoolean();
                            return new QuestionEffect(+15, oneOrTwo ? +1 : +2, false, false);
                        } else {
                            boolean oneOrTwo = rnd.nextBoolean();
                            return new QuestionEffect(-15, oneOrTwo ? -1 : -2, false, false);
                        }

                    case HARD:
                        if (right) {
                            return new QuestionEffect(+20, +2, false, false);
                        } else {
                            return new QuestionEffect(-20, -2, false, false);
                        }

                    case MASTER:
                        if (right) {
                            return new QuestionEffect(+40, +3, false, false);
                        } else {
                            return new QuestionEffect(-40, -3, false, false);
                        }
                }
                break;
        }

        // לא אמור להגיע לכאן
        return new QuestionEffect(0,0,false,false);
    }

    /** אפקט של Surprise לפי רמת קושי והאם טוב/רע */
    private SurpriseEffect computeSurpriseEffect(DifficultyLevel diff, boolean good){
        return switch (diff){
            case EASY ->  good
                    ? new SurpriseEffect(+8, +1)
                    : new SurpriseEffect(-8, -1);
            case MEDIUM -> good
                    ? new SurpriseEffect(+12, +1)
                    : new SurpriseEffect(-12, -1);
            case HARD ->  good
                    ? new SurpriseEffect(+16, +1)
                    : new SurpriseEffect(-16, -1);
        };
    }

    // ======================== Bonus reveal helpers ========================

    private void revealRandomMineBonus(Board b, int playerIdx){
        List<int[]> mines = new ArrayList<>();
        for (int r=0; r<b.rows(); r++){
            for (int c=0; c<b.cols(); c++){
                Cell cell = b.cell(r,c);
                if (!cell.isRevealed() && cell instanceof MineCell){
                    mines.add(new int[]{r,c});
                }
            }
        }
        if (mines.isEmpty()) return;

        int[] chosen = mines.get(rnd.nextInt(mines.size()));
        Cell mine = b.cell(chosen[0], chosen[1]);
        if (!mine.isRevealed()){
            mine.reveal();  // בלי applyRevealScoring → אין נקודות/חיים
        }
    }

    private void revealRandom3x3Bonus(Board b, int playerIdx){
        int R = b.rows(), C = b.cols();
        if (R == 0 || C == 0) return;

        if (R < 3 || C < 3){
            for (int r=0; r<R; r++){
                for (int c=0; c<C; c++){
                    bonusRevealCell(b, playerIdx, r, c);
                }
            }
            return;
        }

        int centerR = 1 + rnd.nextInt(R - 2); // 1..R-2
        int centerC = 1 + rnd.nextInt(C - 2); // 1..C-2

        for (int r=centerR-1; r<=centerR+1; r++){
            for (int c=centerC-1; c<=centerC+1; c++){
                bonusRevealCell(b, playerIdx, r, c);
            }
        }
    }

    private void bonusRevealCell(Board b, int playerIdx, int r, int c){
        Cell cell = b.cell(r,c);
        if (cell.isRevealed()) return;

        cell.reveal(); // אין applyRevealScoring → רק מידע

        if (cell instanceof QuestionCell){
            addPending(playerIdx, new Key(r,c,true));
        } else if (cell instanceof SurpriseCell){
            addPending(playerIdx, new Key(r,c,false));
        }
    }

    // --------- helpers used by the view for "used" question/surprise ---------

    public boolean isQuestionUsed(int playerIdx, int row, int col){
        Board b = (playerIdx==0)? match.board1() : match.board2();
        Cell cell = b.cell(row,col);
        if (!(cell instanceof QuestionCell)) return false;

        Set<Key> pend = pendSet(playerIdx);
        Key qKey = new Key(row,col,true);

        return cell.isRevealed() && !pend.contains(qKey);
    }

    public boolean isSurpriseUsed(int playerIdx, int row, int col){
        Board b = (playerIdx==0)? match.board1() : match.board2();
        Cell cell = b.cell(row,col);
        if (!(cell instanceof SurpriseCell sc)) return false;

        Set<Key> pend = pendSet(playerIdx);
        Key sKey = new Key(row,col,false);

        return sc.wasOperated() || (cell.isRevealed() && !pend.contains(sKey));
    }

}
