package controller;

import javax.swing.SwingUtilities;

import model.DifficultyLevel;
import model.*;
import view.StartListener;
import view.UIStyles;

/**
 * Central application controller (Singleton)
 * Handles navigation between screens and the creation of new matches
 */
public class AppController implements StartListener {

    // ---------- Singleton ----------
    private static AppController INSTANCE;
    private final SysData sys;
    /**
     * Returns the single shared instance of AppController
     * Lazily initializes the instance on first access
     */
    public static synchronized AppController getInstance(){
        if (INSTANCE == null) INSTANCE = new AppController();
        return INSTANCE;
    }

    //Private constructor to prevent direct instantiation
    private AppController(){
    	sys = SysData.getInstance();
    	UIStyles.setAccent(sys.getAccentColor());
    	
    }

    // Shared data store (also singleton)

    // ---------- Entry points / navigation ----------

    //Opens the main menu window on the Swing event-dispatch thread
    public void showMainMenu(){
        SwingUtilities.invokeLater(() -> new view.MainMenuView(this).showSelf());
    }

    /**
     * Callback from the "NewMatchView" when the user presses "Start"
     * Creates a Match with the given players and difficulty and opens
     * the game view with two boards
     * @param p1   name of player 1
     * @param p2   name of player 2
     * @param diff textual representation of the difficulty enum ("EASY"/"MEDIUM"/"HARD")
     */
    @Override
    public void onStart(String p1, String p2, String diff){
        // Convert the chosen difficulty string into the enum value
        DifficultyLevel level = DifficultyLevel.valueOf(diff);

        // Create a new match model object
        Match match = new Match(new Player(p1), new Player(p2), level);

        // Initialize the singleton match controller for this session
        MatchController mc = MatchController.getInstance();
        mc.init(match, sys, this);

        // Open the game view that shows both boards
        new view.GameViewTwoBoards(mc, this).showSelf();
    }
 
    //Opens the "New Match" screen
    public void openNewMatch(){
        if (!hasEnoughQuestionsForMatch()) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Unable to start a new game.\n" +
                    "At least 20 questions must be entered into the system before starting a game.",
                    "Not enough questions",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            new view.MainMenuView(this).showSelf();
            return;
        }

        new view.NewMatchView(this).showSelf();
    }

    private boolean hasEnoughQuestionsForMatch() {
        // checks that there are at least 20 questions
        return sys.questionCount() >= 20;
    }

    //Opens the game history screen which displays previously saved matches
    public void openHistory(){
        new view.HistoryView(sys).showSelf();
    }

    //Opens the question management screen which allows CRUD operations on questions
    public void openQuestionManager(){
        QuestionController qc = QuestionController.getInstance(sys);
        new view.QuestionManagerView(qc).showSelf();
    }
    
    //Opens the game personalization screen 
    public void openPersonalization() {
        new view.PersonalizationView(this).showSelf();
    }
    /**
     * Opens the end-of-game screen summarizing the result of the given record
     * @param rec the game record of the just-finished match
     */
    public void openEndScreen(SysData.GameRecord rec){
        new view.EndView(this, rec).showSelf();
    }
}
