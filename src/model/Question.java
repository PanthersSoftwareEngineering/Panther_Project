package model;

import java.util.List;

/**
 * Represents a multiple-choice question with 4 options
 * Used by QuestionCells in the game and managed by SysData
 */
public class Question {
    /** Unique identifier for the question */
    private final String id;

    /** Question text shown to the player */
    private final String text;

    /** List of 4 answer options */
    private final List<String> options;

    /** Index of the correct answer in the options list (0–3) */
    private final int correctIndex;

    /** Difficulty level of this question */
    private final QuestionLevel level;

    /**
     * Constructs a new Question object
     * @param id           unique question id
     * @param text         question text
     * @param options      list of 4 possible answers
     * @param correctIndex index of the correct answer (0..3)
     * @param level        question difficulty level
     */
    public Question(String id,
                    String text,
                    List<String> options,
                    int correctIndex,
                    QuestionLevel level) {

        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException("Question must have exactly 4 options.");
        }
        if (correctIndex < 0 || correctIndex > 3) {
            throw new IllegalArgumentException("Correct index must be 0–3 (A–D).");
        }

        this.id = id;
        this.text = text;
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
        this.level = level;
    }

    public String id()            { return id; }
    public String text()          { return text; }
    public List<String> options() { return options; }
    public int correctIndex()     { return correctIndex; }
    public QuestionLevel level()  { return level; }

    /** Returns the correct answer as a letter A–D */
    public char correctLetter() {
        return (char) ('A' + correctIndex);
    }
}
