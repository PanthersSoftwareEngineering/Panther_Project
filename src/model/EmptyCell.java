package model;

/**
 * A cell with no adjacent mines and no special behavior.
 * When revealed it appears as a blank space; when flagged it shows a flag.
 */
public class EmptyCell extends Cell {

    @Override
    public CellType type(){ return CellType.EMPTY; }

    @Override
    public String symbol(){
        if (revealed) return " ";
        return flagged ? "🚩" : "";
    }
}
