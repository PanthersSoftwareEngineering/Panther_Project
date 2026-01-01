package model;

/**
 * Immutable snapshot of match state sent to listeners (Views)
 * Keeps UI decoupled from direct access to the live model
 */
public record MatchSnapshot(
        String p1,
        String p2,
        DifficultyLevel level,
        int lives,
        int points,
        int activeIndex,
        long elapsedSeconds,
        boolean finished,
        String[][] boardP1,
        String[][] boardP2
) {}
