package view;

public interface QuestionUI {
    int ask(QuestionDTO q);

    // confirm before activation (question/surprise)
    boolean confirmActivation(String kindLabel, int costPoints);
}
