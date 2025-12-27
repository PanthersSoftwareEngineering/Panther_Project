package model;

public interface CellFactory {
    /**
     * Factory Method:
     * Create a cell of a required type.
     *
     * @param type  requested cell type
     * @param r     row (context)
     * @param c     col (context)
     * @param board board context (for NUMBER, adjacency logic, etc.)
     */
    Cell create(CellType type, int r, int c, Board board);
}
