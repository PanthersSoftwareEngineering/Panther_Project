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

    public static synchronized AppController getInstance(){
        if (INSTANCE == null) INSTANCE = new AppController();
        return INSTANCE;
    }

    private AppController(){} 

    private final SysData sys = SysData.getInstance();
    
    public void showMainMenu(){
        SwingUtilities.invokeLater(() -> new view.MainMenuView(this).showSelf());
    }

}
