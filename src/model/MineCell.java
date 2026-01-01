package model;

/**
 * A cell that hides a mine
 * When revealed, it shows a bomb icon; when flagged, it shows a flag
 */
public class MineCell extends Cell {

    @Override
    public CellType type(){ return CellType.MINE; }

    @Override
    public String symbol(){
        if (revealed) return "💣";
        return flagged ? "🚩" : "";
    }
}
