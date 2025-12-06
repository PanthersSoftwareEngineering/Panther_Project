package controller;

import javax.swing.SwingUtilities;
import model.*;
import view.StartListener;

/**
 * Central application controller (Singleton).
 * Responsible for navigation between screens
 * and for starting new matches 
 */
public class AppController {

    private static AppController INSTANCE;
    private final SysData sys;

    public static synchronized AppController getInstance(){
        if (INSTANCE == null) INSTANCE = new AppController();
        return INSTANCE;
    }

    private AppController(){
    	sys = SysData.getInstance();
    } 

    /**
     * Callback from the "NewMatchView" when the user presses "Start".
     * Creates a Match with the given players and difficulty and opens
     * the game view with two boards.
     *
     * @param p1   name of player 1
     * @param p2   name of player 2
     * @param diff textual representation of the difficulty enum ("EASY"/"MEDIUM"/"HARD")
     */
    public void onStart(String p1, String p2, String diff){
        // Convert the chosen difficulty string into the enum value.
        DifficultyLevel level = DifficultyLevel.valueOf(diff);

        // Create a new match model object.
        Match match = new Match(new Player(p1), new Player(p2), level);

        // Initialize the singleton match controller for this session.
        MatchController mc = MatchController.getInstance();
        mc.init(match, sys, this);

        // Open the game view that shows both boards.
        //new view.GameViewTwoBoards(mc, this).showSelf();// add later
    }
 
    public void openQuestionManager(){
        QuestionController qc = QuestionController.getInstance(sys);
        new view.QuestionManagerView(qc).showSelf();
    }
    
    public void showMainMenu(){
        SwingUtilities.invokeLater(() -> new view.MainMenuView(this).showSelf());
    }
    
    public void openEndScreen(SysData.GameRecord rec){
        new view.EndView(this, rec).showSelf();
    }

}
