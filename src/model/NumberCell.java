package model;

/**
 * A cell representing the number of adjacent mines (1..8).
 */
public class NumberCell extends Cell {
    /** Number of adjacent mines (must be between 1 and 8). */
    private final int value;

    /**
     * Creates a new NumberCell with the given value.
     *
     * @param value number of adjacent mines (1..8)
     */
    public NumberCell(int value){
        if(value < 1 || value > 8)
            throw new IllegalArgumentException("1..8");
        this.value = value;
    }

    /** @return the numeric value of this cell. */
    public int value(){ return value; }

    @Override
    public CellType type(){ return CellType.NUMBER; }

    @Override
    public String symbol(){
        if (revealed) return String.valueOf(value);
        return flagged ? "🚩" : "";
    }
}
