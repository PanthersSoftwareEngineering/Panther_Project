package view;

/**
 * Abstraction for any UI element that can ask a multiple-choice question.
 * Implemented by the GameView so the controller can trigger questions
 * without depending directly on Swing.
 */
public interface QuestionUI {
    /**
     * Shows the given question to the user and returns their chosen answer.
     *
     * @param q question data
     * @return chosen index [0..3], or -1 if the user cancelled or closed the dialog
     */
    int ask(QuestionDTO q);
}
