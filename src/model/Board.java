package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Board holds a 2D grid of Cells
 * Uses Factory Method via CellFactory, so Board does not "new MineCell()" etc directly
 */
public class Board {

    private final int rows;
    private final int cols;
    private final Cell[][] grid;

    private final CellFactory factory;
    private final Random rnd = new Random();

    // ---------------- Constructors ----------------

    /** Keeps original API: Board(level) */
    public Board(DifficultyLevel level) {
        this(level, new DefaultCellFactory());
    }

    /** Overload that allows injecting a factory */
    public Board(DifficultyLevel level, CellFactory factory) {
        this.factory = (factory != null) ? factory : new DefaultCellFactory();

        this.rows = DifficultyConfig.getRows(level);
        this.cols = DifficultyConfig.getCols(level);

        int mines     = DifficultyConfig.getMines(level);
        int questions = DifficultyConfig.getQuestionCells(level);
        int surprises = DifficultyConfig.getSurpriseCells(level);

        this.grid = new Cell[rows][cols];

        initEmptyGrid();
        placeRandom(CellType.MINE, mines);
        placeRandom(CellType.QUESTION, questions);
        placeRandom(CellType.SURPRISE, surprises);

        // After placing specials, compute numbers (NUMBER factory decides EMPTY/NUMBER)
        fillNumbers();
    }

    /**
     * Extra constructor useful for tests (small custom board)
     * Default = all empty
     */
    public Board(int rows, int cols) {
        this(rows, cols, new DefaultCellFactory());
    }

    public Board(int rows, int cols, CellFactory factory) {
        this.rows = rows;
        this.cols = cols;
        this.factory = (factory != null) ? factory : new DefaultCellFactory();
        this.grid = new Cell[rows][cols];
        initEmptyGrid();
    }

    // ---------------- Public API ----------------

    public int rows() { return rows; }
    public int cols() { return cols; }

    public Cell cell(int r, int c) {
        return grid[r][c];
    }

    /**
     * Optional helper for unit tests
     */
    public void setCell(int r, int c, Cell newCell) {
        grid[r][c] = newCell;
    }

    // ---------------- Init helpers ----------------

    private void initEmptyGrid() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = factory.create(CellType.EMPTY, r, c, this);
            }
        }
    }

    /**
     * Place N cells of a given type in random empty positions
     */
    private void placeRandom(CellType type, int count) {
        if (count <= 0) return;

        List<int[]> free = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // We treat "EmptyCell" as free spot
                if (grid[r][c] instanceof EmptyCell) {
                    free.add(new int[]{r, c});
                }
            }
        }
        Collections.shuffle(free, rnd);

        int placed = 0;
        for (int[] pos : free) {
            if (placed >= count) break;
            int r = pos[0], c = pos[1];
            grid[r][c] = factory.create(type, r, c, this);
            placed++;
        }
    }

    /**
     * Convert all non-mine / non-question / non-surprise cells into NUMBER-or-EMPTY
     * The factory returns EmptyCell when adjacent mines count is 0
     */
    private void fillNumbers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cur = grid[r][c];
                if (cur instanceof MineCell) continue;
                if (cur instanceof QuestionCell) continue;
                if (cur instanceof SurpriseCell) continue;

                // NUMBER factory will decide EMPTY/NUMBER based on adjacency
                grid[r][c] = factory.create(CellType.NUMBER, r, c, this);
            }
        }
    }
	 // =====================
	 // Testing helpers ONLY
	 // =====================
	
	 /**
	  * Allows tests to inject a specific cell into the board
	  */
	 public void setCellForTest(int row, int col, Cell cell) {
	     if (row < 0 || row >= rows || col < 0 || col >= cols) {
	         throw new IllegalArgumentException("Invalid cell position");
	     }
	     grid[row][col] = cell;
	 }

    // ---------------- Adjacency ----------------

    public int countAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (grid[nr][nc] instanceof MineCell) count++;
            }
        }
        return count;
    }
    
    
}
