package model;

import java.util.Random;

/**
 * Represents a single Minesweeper board.
 * Responsible for generating cells according to difficulty,
 * placing mines, numbers, question cells, surprise cells, and empty cells.
 */
public class Board {
    /** Number of rows in the board. */
    private final int rows;

    /** Number of columns in the board. */
    private final int cols;

    /** 2D grid of cells (each position holds a Cell subclass). */
    private final Cell[][] grid;

    /** Random generator used for placing mines, questions, and surprises. */
    private final Random rnd = new Random();

    /**
     * Creates a board for a given difficulty level.
     * Uses DifficultyConfig to determine board size and counts and then generates the grid.
     */
    public Board(DifficultyLevel level) {
        this.rows = DifficultyConfig.getRows(level);
        this.cols = DifficultyConfig.getCols(level);
        this.grid = new Cell[rows][cols];

        int mines         = DifficultyConfig.getMines(level);
        int questionCells = DifficultyConfig.getQuestionCells(level);
        int surpriseCells = DifficultyConfig.getSurpriseCells(level);

        generate(mines, questionCells, surpriseCells);
    }

    /** @return number of rows. */
    public int rows() { return rows; }

    /** @return number of columns. */
    public int cols() { return cols; }

    /**
     * Returns the cell at the given row and column.
     *
     * @param r row index
     * @param c column index
     */
    public Cell cell(int r, int c) { return grid[r][c]; }

    /**
     * Generates the entire board by placing:
     * 1. Mines
     * 2. Number cells around mines
     * 3. Question cells
     * 4. Surprise cells
     * 5. Empty cells filling remaining spaces
     */
    private void generate(int mines, int questionCells, int surpriseCells) {
        placeMines(mines);
        placeNumbers();
        placeQuestions(questionCells);
        placeSurprises(surpriseCells);
        fillEmpties();
    }

    /** Randomly places a given number of mines on the board. */
    private void placeMines(int count) {
        int placed = 0;
        while (placed < count) {
            int r = rnd.nextInt(rows), c = rnd.nextInt(cols);
            if (grid[r][c] == null) {
                grid[r][c] = new MineCell();
                placed++;
            }
        }
    }

    /**
     * For every cell that is still empty, counts adjacent mines and,
     * if the count is > 0, places a NumberCell with the corresponding value.
     */
    private void placeNumbers() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == null) {
                    int adj = countAdjMines(r, c);
                    if (adj > 0) grid[r][c] = new NumberCell(adj);
                }
            }
    }

    /**
     * Randomly places a given number of QuestionCells on empty spots.
     * The specific Question that will be asked is chosen later by MatchController
     * from SysData using random selection.
     */
    private void placeQuestions(int count) {
        int placed = 0;
        while (placed < count) {
            int r = rnd.nextInt(rows), c = rnd.nextInt(cols);
            if (grid[r][c] == null) {
                grid[r][c] = new QuestionCell();
                placed++;
            }
        }
    }

    /** Randomly places a given number of SurpriseCells on empty spots. */
    private void placeSurprises(int count) {
        int placed = 0;
        while (placed < count) {
            int r = rnd.nextInt(rows), c = rnd.nextInt(cols);
            if (grid[r][c] == null) {
                grid[r][c] = new SurpriseCell();
                placed++;
            }
        }
    }

    /** Fills any remaining null cells with EmptyCell instances. */
    private void fillEmpties() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (grid[r][c] == null)
                    grid[r][c] = new EmptyCell();
    }

    /** Counts how many mines are adjacent to the given cell (8-direction). */
    private int countAdjMines(int r, int c) {
        int cnt = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                        && (grid[nr][nc] instanceof MineCell))
                    cnt++;
            }
        return cnt;
    }
}
