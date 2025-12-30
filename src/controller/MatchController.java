package controller;

import model.*;
import view.QuestionDTO;
import view.QuestionUI;

import java.util.*;

/**
 * MatchController
 * ==============
 * Controller for a single match (Singleton).
 *
 * IMPORTANT FIX:
 * We do NOT rely on Match to notify observers.
 * The controller owns a listener list and publishes snapshots after every state change.
 */
public class MatchController {

    // ---------- Singleton ----------
    private static MatchController INSTANCE;

    public static synchronized MatchController getInstance() {
        if (INSTANCE == null) INSTANCE = new MatchController();
        return INSTANCE;
    }

    private MatchController() {}

    // ---------- Current session state ----------
    private Match match;
    private SysData sys;
    private AppController app;
    private QuestionUI questionUI;

    private final Random rnd = new Random();

    private static final int[] DR = {-1,-1,-1,0,0,1,1,1};
    private static final int[] DC = {-1, 0, 1,-1,1,-1,0,1};

    private SysData.GameRecord lastRecord = null;
    public SysData.GameRecord getLastRecord(){ return lastRecord; }

    // ✅ Controller-managed observers
    private final List<MatchListener> listeners = new ArrayList<>();

    private void publish() {
        if (match == null) return;
        MatchSnapshot s = match.snapshot();
        for (MatchListener l : new ArrayList<>(listeners)) {
            l.onMatchChanged(s);
        }
    }

    // ✅ Time runs always (independent of Match implementation)
    private long matchStartMillis = 0;
    private long frozenElapsedSeconds = -1;

    public long getElapsedSeconds(){
        if (frozenElapsedSeconds >= 0) return frozenElapsedSeconds;
        if (matchStartMillis == 0) return 0;
        return (System.currentTimeMillis() - matchStartMillis) / 1000;
    }

    // ✅ For QuestionDialog coloring
    private int lastQuestionCorrectIndex = -1;
    public int getLastQuestionCorrectIndex(){ return lastQuestionCorrectIndex; }

    // ✅ Interaction breakdown for Toast
    private int lastActivationCost = 0;   // positive number (5/8/12)
    private int lastEffectPoints   = 0;
    private int lastEffectLives    = 0;
    private int lastNetPoints      = 0;
    private int lastNetLives       = 0;
    private String lastInteractionMessage = null;

    public int getLastActivationCost(){ return lastActivationCost; }
    public int getLastEffectPoints(){ return lastEffectPoints; }
    public int getLastEffectLives(){ return lastEffectLives; }
    public int getLastNetPoints(){ return lastNetPoints; }
    public int getLastNetLives(){ return lastNetLives; }

    private void setLastInteraction(int cost, int effPts, int effLives, String msg){
        lastActivationCost = cost;
        lastEffectPoints   = effPts;
        lastEffectLives    = effLives;
        lastNetPoints      = -cost + effPts;
        lastNetLives       = effLives;
        lastInteractionMessage = msg;
    }

    public String consumeLastInteractionMessage(){
        String s = lastInteractionMessage;
        lastInteractionMessage = null;
        return s;
    }

    // ---------- Pending interactions ----------
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

    private static final class QuestionEffect {
        final int pointsDelta;
        final int livesDelta;
        final boolean revealMineBonus;
        final boolean revealAreaBonus;

        QuestionEffect(int p,int l,boolean mine,boolean area){
            this.pointsDelta = p;
            this.livesDelta  = l;
            this.revealMineBonus = mine;
            this.revealAreaBonus = area;
        }
    }

    private static final class SurpriseEffect {
        final int pointsDelta;
        final int livesDelta;
        SurpriseEffect(int p,int l){ pointsDelta=p; livesDelta=l; }
    }

    // ---------- init/reset ----------
    public synchronized void init(Match match, SysData sys, AppController app){
        this.match = match;
        this.sys   = sys;
        this.app   = app;
        this.questionUI = null;

        pendingP1.clear();
        pendingP2.clear();

        lastRecord = null;

        lastQuestionCorrectIndex = -1;
        setLastInteraction(0,0,0,null);

        matchStartMillis = System.currentTimeMillis();
        frozenElapsedSeconds = -1;

        publish(); // initial snapshot
    }

    public void setQuestionUI(QuestionUI ui){
        this.questionUI = ui;
    }

    // ---------- Observers ----------
    public void addMatchListener(MatchListener l){
        if (l == null) return;
        if (!listeners.contains(l)) listeners.add(l);
        if (match != null) l.onMatchChanged(match.snapshot());
    }

    public void removeMatchListener(MatchListener l){
        listeners.remove(l);
    }

    // ---------- Read-only accessors ----------
    public boolean isFinished(){ return match != null && match.isFinished(); }
    public String  getP1(){ return match.player1().name(); }
    public String  getP2(){ return match.player2().name(); }
    public String  getDiff(){ return match.level().name(); }
    public int     getLives(){ return match.lives(); }
    public int     getPoints(){ return match.points(); }
    public boolean isPlayer1Active(){ return match.activeIndex() == 0; }
    public int     rows(){ return match.board1().rows(); }
    public int     cols(){ return match.board1().cols(); }

    public boolean isRevealed(int playerIdx,int row,int col){
        Board b = (playerIdx==0)? match.board1() : match.board2();
        return b.cell(row,col).isRevealed();
    }

    public boolean isFlagged(int playerIdx, int row, int col){
        Board b = (playerIdx==0)? match.board1() : match.board2();
        return b.cell(row,col).isFlagged();
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

        if (cell instanceof EmptyCell || cell instanceof QuestionCell || cell instanceof SurpriseCell) {
            floodReveal(b, playerIdx, row, col);
        } else {
            cell.reveal();
            applyRevealScoring(cell);
        }

        match.checkFinish();
        if (match.isFinished()) {
            finishAndClose();
            publish();
            return;
        }

        endTurn();
        publish();
    }

    /**
     * Tries to interact with a pending Question/SURPRISE cell.
     * ✅ confirm first, then pay activation cost, then apply effect
     * ✅ stores lastInteraction fields for the view toast
     */
    public boolean tryInteract(int playerIdx,int row,int col){
        if (playerIdx != match.activeIndex()) return false;

        Set<Key> pend = pendSet(playerIdx);
        Key qKey = new Key(row,col,true);
        Key sKey = new Key(row,col,false);

        Board b = (playerIdx==0)? match.board1() : match.board2();
        Cell  cell = b.cell(row,col);

        // ---------- Question ----------
        if (pend.contains(qKey) && cell instanceof QuestionCell){

            setLastInteraction(0,0,0,null);
            lastQuestionCorrectIndex = -1;

            int cost = activationCost(match.level());

            boolean ok = (questionUI == null) || questionUI.confirmActivation("Question", cost);
            if (!ok) return false;

            // pay cost first
            match.addPoints(-cost);

            Question q = sys.drawRandomQuestion();
            boolean right;
            QuestionLevel qLevel;
            int correctIdxForMsg = -1;

            if (q == null){
                right = true;
                qLevel = QuestionLevel.EASY;
            } else {
                lastQuestionCorrectIndex = q.correctIndex();
                correctIdxForMsg = q.correctIndex();

                QuestionDTO dto = new QuestionDTO(q.id(), q.text(), q.options(), q.level().name());
                int choice = (questionUI != null) ? questionUI.ask(dto) : 0;

                // no cancel in dialog now -> but keep safe
                if (choice < 0) choice = 0;

                right = (choice == q.correctIndex());
                qLevel = q.level();
            }

            QuestionEffect eff = computeQuestionEffect(match.level(), qLevel, right);

            match.addPoints(eff.pointsDelta);
            match.addLives (eff.livesDelta);

            if (eff.revealMineBonus) revealRandomMineBonus(b);
            if (eff.revealAreaBonus) revealRandom3x3Bonus(b, playerIdx);

            String base = right ? "✅ Correct!" : "❌ Wrong!";
            if (!right && correctIdxForMsg >= 0){
                base += " Correct: " + (char)('A' + correctIdxForMsg);
            }
            setLastInteraction(cost, eff.pointsDelta, eff.livesDelta, base);

            pend.remove(qKey);

            match.checkFinish();
            if (match.isFinished()) {
                finishAndClose();
                publish();
                return true;
            }

            endTurn();
            publish();
            return true;
        }

        // ---------- Surprise ----------
        if (pend.contains(sKey) && cell instanceof SurpriseCell sc){

            setLastInteraction(0,0,0,null);

            if (!sc.isRevealed() || sc.wasOperated()){
                pend.remove(sKey);
                publish();
                return false;
            }

            int cost = activationCost(match.level());
            boolean ok = (questionUI == null) || questionUI.confirmActivation("Surprise", cost);
            if (!ok) return false;

            // pay cost first
            match.addPoints(-cost);

            sc.operate();

            boolean good = rnd.nextBoolean();
            SurpriseEffect se = computeSurpriseEffect(match.level(), good);

            match.addPoints(se.pointsDelta);
            match.addLives (se.livesDelta);

            String base = good ? "🎁 Good surprise!" : "🎁 Bad surprise!";
            setLastInteraction(cost, se.pointsDelta, se.livesDelta, base);

            pend.remove(sKey);

            match.checkFinish();
            if (match.isFinished()) {
                finishAndClose();
                publish();
                return true;
            }

            endTurn();
            publish();
            return true;
        }

        return false;
    }

    /**
     * Flag rules requested:
     * ✅ placing a flag ends turn
     * ✅ removing a flag does NOT end turn (so you can correct it)
     * ✅ always publish (so UI never gets stuck)
     */
    public void toggleFlag(int playerIndex,int row,int col){
        if (playerIndex != match.activeIndex()) return;

        Board b = (playerIndex==0)? match.board1() : match.board2();
        Cell  cell = b.cell(row,col);

        boolean before = cell.isFlagged();
        cell.toggleFlag();
        boolean after = cell.isFlagged();

        if (before != after) {
            applyFlagScoring(cell, after);
        }

        match.checkFinish();
        if (match.isFinished()) {
            finishAndClose();
            publish();
            return;
        }

        // ✅ לפי הדרישה: דגל לא מסיים תור (לא משנה אם שמתי/הסרתי)
        publish();
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

            if (cell instanceof QuestionCell) addPending(playerIdx,new Key(r,c,true));
            if (cell instanceof SurpriseCell) addPending(playerIdx,new Key(r,c,false));

            if (cell instanceof NumberCell) continue;
            if (cell instanceof MineCell)   continue;

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
        frozenElapsedSeconds = getElapsedSeconds();

        boolean won = match.lives() > 0;

        match.convertLivesToPoints();
        revealAllBoards();

        SysData.GameRecord rec = match.toRecord(won);
        sys.addRecord(rec);

        lastRecord = rec;
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

    private void endTurn(){
        match.endTurn();
    }

    private Set<Key> pendSet(int idx){
        return idx==0 ? pendingP1 : pendingP2;
    }

    private void addPending(int idx, Key k){
        pendSet(idx).add(k);
    }

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

    // ======================== Effects ========================

    private int activationCost(DifficultyLevel diff){
        return switch (diff){
            case EASY   -> 5;
            case MEDIUM -> 8;
            case HARD   -> 12;
        };
    }

    private QuestionEffect computeQuestionEffect(DifficultyLevel diff, QuestionLevel ql, boolean right){
        switch (diff){
            case EASY:
                switch (ql){
                    case EASY:
                        if (right) return new QuestionEffect(+3, +1, false, false);
                        boolean punish = rnd.nextBoolean();
                        return punish ? new QuestionEffect(-3, 0, false, false)
                                      : new QuestionEffect(0, 0, false, false);

                    case MEDIUM:
                        if (right) return new QuestionEffect(+6, 0, true, false);
                        punish = rnd.nextBoolean();
                        return punish ? new QuestionEffect(-6, 0, false, false)
                                      : new QuestionEffect(0, 0, false, false);

                    case HARD:
                        if (right) return new QuestionEffect(+10, 0, false, true);
                        return new QuestionEffect(-10, 0, false, false);

                    case MASTER:
                        if (right) return new QuestionEffect(+15, +2, false, false);
                        return new QuestionEffect(-15, -1, false, false);
                }

            case MEDIUM:
                switch (ql){
                    case EASY:
                        if (right) return new QuestionEffect(+8, +1, false, false);
                        return new QuestionEffect(-8, 0, false, false);

                    case MEDIUM:
                        if (right) return new QuestionEffect(+10, +1, false, false);
                        boolean punish = rnd.nextBoolean();
                        return punish ? new QuestionEffect(-10, -1, false, false)
                                      : new QuestionEffect(0, 0, false, false);

                    case HARD:
                        if (right) return new QuestionEffect(+15, +1, false, false);
                        return new QuestionEffect(-15, -1, false, false);

                    case MASTER:
                        if (right) return new QuestionEffect(+20, +2, false, false);
                        boolean oneOrTwo = rnd.nextBoolean();
                        return new QuestionEffect(-20, oneOrTwo ? -1 : -2, false, false);
                }

            case HARD:
                switch (ql){
                    case EASY:
                        if (right) return new QuestionEffect(+10, +1, false, false);
                        return new QuestionEffect(-10, -1, false, false);

                    case MEDIUM:
                        boolean oneOrTwo = rnd.nextBoolean();
                        if (right) return new QuestionEffect(+15, oneOrTwo ? +1 : +2, false, false);
                        return new QuestionEffect(-15, oneOrTwo ? -1 : -2, false, false);

                    case HARD:
                        if (right) return new QuestionEffect(+20, +2, false, false);
                        return new QuestionEffect(-20, -2, false, false);

                    case MASTER:
                        if (right) return new QuestionEffect(+40, +3, false, false);
                        return new QuestionEffect(-40, -3, false, false);
                }
        }
        return new QuestionEffect(0,0,false,false);
    }

    private SurpriseEffect computeSurpriseEffect(DifficultyLevel diff, boolean good){
        return switch (diff){
            case EASY   -> good ? new SurpriseEffect(+8, +1)   : new SurpriseEffect(-8, -1);
            case MEDIUM -> good ? new SurpriseEffect(+12, +1)  : new SurpriseEffect(-12, -1);
            case HARD   -> good ? new SurpriseEffect(+16, +1)  : new SurpriseEffect(-16, -1);
        };
    }

    // ======================== Bonus reveals ========================

    private void revealRandomMineBonus(Board b){
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
        b.cell(chosen[0], chosen[1]).reveal();
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

        int centerR = 1 + rnd.nextInt(R - 2);
        int centerC = 1 + rnd.nextInt(C - 2);

        for (int r=centerR-1; r<=centerR+1; r++){
            for (int c=centerC-1; c<=centerC+1; c++){
                bonusRevealCell(b, playerIdx, r, c);
            }
        }
    }

    private void bonusRevealCell(Board b, int playerIdx, int r, int c){
        Cell cell = b.cell(r,c);
        if (cell.isRevealed()) return;

        cell.reveal();

        if (cell instanceof QuestionCell){
            addPending(playerIdx, new Key(r,c,true));
        } else if (cell instanceof SurpriseCell){
            addPending(playerIdx, new Key(r,c,false));
        }
    }

    // --------- used checks (view needs these) ---------

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
