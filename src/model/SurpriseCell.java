package model;

/**
 * A cell that triggers a random surprise effect (good or bad),
 * such as adding or subtracting points.
 */
public class SurpriseCell extends Cell {
    /** Indicates whether the surprise effect has already been applied. */
    private boolean operated = false;

    /**
     * @return true if this surprise has been activated already.
     */
    public boolean wasOperated(){ return operated; }

    /**
     * Attempts to activate the surprise.
     * Only works if the cell is revealed and not yet operated.
     *
     * @return true if the surprise was operated this call, false otherwise
     */
    public boolean operate(){
        if (!revealed || operated) return false;
        operated = true;
        return true;
    }

    @Override
    public CellType type(){ return CellType.SURPRISE; }

    @Override
    public String symbol(){
        if (revealed) return "🎁";
        return flagged ? "🚩" : "";
    }
}
