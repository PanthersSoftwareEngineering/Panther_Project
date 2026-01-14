package controller;

import java.util.List;
import model.Question;
import model.SysData;

/**
 * Question management controller (Singleton)
 * Acts as an MVC layer between UI and SysData
 */
public class QuestionController {

    /* Singleton instance field */
    private static QuestionController INSTANCE;

    /* Singleton accessor */
    public static synchronized QuestionController getInstance(SysData sys) {
        if (INSTANCE == null)
            INSTANCE = new QuestionController(sys);
        return INSTANCE;
    }

    /* Reference to SysData */
    private final SysData sys;

    /* Private constructor */
    private QuestionController(SysData sys){
        this.sys = sys;
    }

    /* Supplies questions list to the table model */
    public List<Question> list(){
        return sys.questions();
    }

    /* Add a question and persist */
    public void add(Question q){
        sys.addQuestion(q);
    }

    /* Delete question by id with  */
    public boolean delete(String id){
        return sys.deleteQuestion(id);
    }

    /*  Replace existing question (edit) */
    public void replace(String oldId, Question updated){
        sys.replaceQuestion(oldId, updated);  
    }

    /* Provide next numeric ID for auto numbering in UI if needed */
    public int nextId(){
        return sys.nextQuestionId();
    }
}