package model;

/**
 * A cell that triggers a trivia question when interacted with
 * The concrete question is chosen randomly from the question bank (SysData),
 * so this cell does not store a QuestionLevel
 */
public class QuestionCell extends Cell {

    @Override
    public CellType type() { return CellType.QUESTION; }

    @Override
    public String symbol() {
        if (revealed) return "?";
        return flagged ? "🚩" : "";
    }
}
