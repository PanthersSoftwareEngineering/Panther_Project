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
