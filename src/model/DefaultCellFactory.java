package model;

public class DefaultCellFactory implements CellFactory {

    @Override
    public Cell create(CellType type, int r, int c, Board board) {
        return switch (type) {
            case EMPTY    -> new EmptyCell();
            case MINE     -> new MineCell();
            case QUESTION -> new QuestionCell();
            case SURPRISE -> new SurpriseCell();

            case NUMBER -> {
                // NUMBER depends on adjacency (mines around)
                int n = board.countAdjacentMines(r, c);
                yield (n == 0) ? new EmptyCell() : new NumberCell(n);
            }
        };
    }
}
