package model;

/**
 * Abstract base class for all cell types on the board.
 * Provides common state and behavior for:
 * - revealed/hidden
 * - flagged/unflagged
 * Subclasses specify their type and symbol.
 */
public abstract class Cell {
    /** Indicates whether the cell has been revealed to the player. */
    protected boolean revealed = false;

    /** Indicates whether the cell is currently flagged by the player. */
    protected boolean flagged = false;

    /** @return true if this cell is revealed. */
    public boolean isRevealed(){ return revealed; }

    /** @return true if this cell is currently flagged. */
    public boolean isFlagged(){ return flagged; }

    /**
     * Reveals the cell.
     * Once revealed, the cell can no longer be flagged.
     */
    public void reveal(){
        if(!revealed){
            revealed = true;
            flagged = false;
        }
    }

    /**
     * Toggles the flag state if the cell is not yet revealed.
     */
    public void toggleFlag(){
        if(!revealed)
            flagged = !flagged;
    }

    /**
     * Returns the logical type of the cell (mine, empty, number, question, surprise).
     */
    public abstract CellType type();

    /**
     * Returns the symbol to show in the UI when this cell is revealed (or flagged).
     */
    public abstract String symbol();
}
