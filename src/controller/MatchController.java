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

    // ---------- Question effect holder (points + lives + bonuses) ----------
    private static final class QuestionEffect {
        final int pointsDelta;
        final int livesDelta;
        final boolean revealMineBonus;   // חשיפת מוקש (ללא ניקוד) – EASY+MEDIUM+right
        final boolean revealAreaBonus;   // חשיפת 3x3 (ללא ניקוד) – EASY+HARD+right

        QuestionEffect(int p,int l,boolean mine,boolean area){
            this.pointsDelta = p;
            this.livesDelta  = l;
            this.revealMineBonus = mine;
            this.revealAreaBonus = area;
        }
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

                    // ====== זה החלק החדש ======
                    // cancel (או סגירת חלון) → לא לענות, לא לשנות תור
                    if (choice < 0){
                        // משאירים את התא כ-pending לאותו שחקן
                        return false;
                    }
                    // ==========================

                    right  = (choice == q.correctIndex());
                    qLevel = q.level();
                } else {
                    // אין UI – נניח תשובה נכונה (אפשר לשנות לפי הצורך)
                    right  = true;
                    qLevel = q.level();
                }
            }

            // אפקט מלא לפי הטבלה (נקודות + לבבות + בונוסים)
            DifficultyLevel diff = match.level();
            QuestionEffect eff = computeQuestionEffect(diff, qLevel, right);

            match.addPoints(eff.pointsDelta);
            match.addLives (eff.livesDelta);

            if (eff.revealMineBonus){
                //revealRandomMineBonus(b, playerIdx);//implement later
            }
            if (eff.revealAreaBonus){
                //revealRandom3x3Bonus(b, playerIdx);//implement later
            }

            // השאלה טופלה – מסירים מה-pending
            pend.remove(qKey);

        // ----- Surprise cell -----
        } else if (pend.contains(sKey) && cell instanceof SurpriseCell sc){
            if (sc.isRevealed() && !sc.wasOperated()){
                sc.operate();
                boolean good = rnd.nextBoolean();
                int delta = good ? +5 : -5;
                match.addPoints(delta);
                lastSurpriseMessage = good
                        ? "🎁 Good surprise! +5 points"
                        : "🎁 Bad surprise! -5 points";
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

            // מוקש לעולם לא ניכנס אליו בהצפה (בגלל בדיקה למטה),
            // אבל אם איכשהו הגענו – לא נרחיב ממנו.
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

    // ======================== Question scoring (from table) ========================

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
                            // (+3pts) AND (+1life)
                            return new QuestionEffect(+3, +1, false, false);
                        } else {
                            // (-3pts) OR nothing
                            boolean punish = rnd.nextBoolean();
                            return punish
                                    ? new QuestionEffect(-3, 0, false, false)
                                    : new QuestionEffect(0, 0, false, false);
                        }

                    case MEDIUM:
                        if (right) {
                            // (+6pts) AND חשיפת משבצת מוקש (ללא ניקוד על החשיפה)
                            return new QuestionEffect(+6, 0, true, false);
                        } else {
                            // (-6pts) OR nothing
                            boolean punish = rnd.nextBoolean();
                            return punish
                                    ? new QuestionEffect(-6, 0, false, false)
                                    : new QuestionEffect(0, 0, false, false);
                        }

                    case HARD:
                        if (right) {
                            // (+10pts) AND חשיפת 3x3 (ללא ניקוד/לב)
                            return new QuestionEffect(+10, 0, false, true);
                        } else {
                            // -10pts
                            return new QuestionEffect(-10, 0, false, false);
                        }

                    case MASTER:
                        if (right) {
                            // (+15pts) AND (+1life)
                            return new QuestionEffect(+15, +1, false, false);
                        } else {
                            // (-15pts) AND (-1life)
                            return new QuestionEffect(-15, -1, false, false);
                        }
                }
                break;

            case MEDIUM:
                switch (ql){
                    case EASY:
                        if (right) {
                            // (+8pts) AND (+1life)
                            return new QuestionEffect(+8, +1, false, false);
                        } else {
                            // (-8pts)
                            return new QuestionEffect(-8, 0, false, false);
                        }

                    case MEDIUM:
                        if (right) {
                            // (+10pts) AND (+1life)
                            return new QuestionEffect(+10, +1, false, false);
                        } else {
                            // ((-10pts) AND (-1life)) OR nothing
                            boolean punish = rnd.nextBoolean();
                            if (punish){
                                return new QuestionEffect(-10, -1, false, false);
                            } else {
                                return new QuestionEffect(0, 0, false, false);
                            }
                        }

                    case HARD:
                        if (right) {
                            // (+15pts) AND (+1life)
                            return new QuestionEffect(+15, +1, false, false);
                        } else {
                            // (-15pts) AND (-1life)
                            return new QuestionEffect(-15, -1, false, false);
                        }

                    case MASTER:
                        if (right) {
                            // (+20pts) AND (+2lives)
                            return new QuestionEffect(+20, +2, false, false);
                        } else {
                            // ((-20pts) AND (-1life)) OR ((-20pts) AND (-2lives))
                            boolean oneOrTwo = rnd.nextBoolean();
                            return new QuestionEffect(-20, oneOrTwo ? -1 : -2, false, false);
                        }
                }
                break;

            case HARD:
                switch (ql){
                    case EASY:
                        if (right) {
                            // (+10pts) AND (+1life)
                            return new QuestionEffect(+10, +1, false, false);
                        } else {
                            // (-10pts) AND (-1life)
                            return new QuestionEffect(-10, -1, false, false);
                        }

                    case MEDIUM:
                        if (right) {
                            // ((+15pts) AND (+1life)) OR ((+15pts) AND (+2lives))
                            boolean oneOrTwo = rnd.nextBoolean();
                            return new QuestionEffect(+15, oneOrTwo ? +1 : +2, false, false);
                        } else {
                            // ((-15pts) AND (-1life)) OR ((-15pts) AND (-2lives))
                            boolean oneOrTwo = rnd.nextBoolean();
                            return new QuestionEffect(-15, oneOrTwo ? -1 : -2, false, false);
                        }

                    case HARD:
                        if (right) {
                            // (+20pts) AND (+2lives)
                            return new QuestionEffect(+20, +2, false, false);
                        } else {
                            // (-20pts) AND (-2lives)
                            return new QuestionEffect(-20, -2, false, false);
                        }

                    case MASTER:
                        if (right) {
                            // (+40pts) AND (+3lives)
                            return new QuestionEffect(+40, +3, false, false);
                        } else {
                            // (-40pts) AND (-3lives)
                            return new QuestionEffect(-40, -3, false, false);
                        }
                }
                break;
        }

        // לא אמור להגיע לכאן
        return new QuestionEffect(0,0,false,false);
    }
}
