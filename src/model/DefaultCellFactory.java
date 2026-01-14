package model;

/**
 * Implementation of the CellFactory interface.
 * This class follows the Factory Design Pattern to encapsulate the instantiation 
 * logic for different types of game cells.
 */
public class DefaultCellFactory implements CellFactory {

    @Override
    public Cell create(CellType type, int r, int c, Board board) {
        return switch (type) {
            case EMPTY    -> new EmptyCell();
            case MINE     -> new MineCell();
            case QUESTION -> new QuestionCell();
            case SURPRISE -> new SurpriseCell();

            case NUMBER -> {
                // NUMBER depends on adjacency (mines around) - meaning:
            	// Determine the cell value based on surrounding mines (1-8)
                // If the count is 0, it is treated as an EmptyCell
                int n = board.countAdjacentMines(r, c);
                yield (n == 0) ? new EmptyCell() : new NumberCell(n);
            }
        };
    }
}
