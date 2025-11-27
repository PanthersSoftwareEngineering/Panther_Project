package model;

/**
 * Utility class that holds configuration values for each DifficultyLevel.
 * This keeps DifficultyLevel itself simple (no fields/constructors).
 * All settings (board size, number of mines, questions, surprises, and lives)
 * are centralized here.
 */
public class DifficultyConfig {

    /**
     * Returns the number of rows for the given difficulty.
     */
    public static int getRows(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 9;
            case MEDIUM: return 13;
            case HARD:   return 16;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the number of columns for the given difficulty.
     */
    public static int getCols(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 9;
            case MEDIUM: return 13;
            case HARD:   return 16;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the number of mines to place for the given difficulty.
     */
    public static int getMines(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 10;
            case MEDIUM: return 26;
            case HARD:   return 44;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the number of question cells to place for the given difficulty.
     */
    public static int getQuestionCells(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 6;
            case MEDIUM: return 7;
            case HARD:   return 11;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the number of surprise cells to place for the given difficulty.
     */
    public static int getSurpriseCells(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 2;
            case MEDIUM: return 3;
            case HARD:   return 4;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the number of starting lives for the given difficulty.
     */
    public static int getStartingLives(DifficultyLevel level) {
        switch (level) {
            case EASY:   return 10;
            case MEDIUM: return 8;
            case HARD:   return 6;
        }
        throw new IllegalArgumentException();
    }
}
