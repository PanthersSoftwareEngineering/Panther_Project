package controller;

import javax.swing.SwingUtilities;

/*import model.*;
import view.StartListener;*/

public class AppController /*implements StartListener*/ {

    // [Iter2] – תשתית Singleton כללית לבקר הראשי
    private static AppController INSTANCE;
    public static synchronized AppController getInstance(){
        if (INSTANCE == null) INSTANCE = new AppController();
        return INSTANCE;
    }

    // [Iter2] – ctor פרטי של ה-Singleton
    private AppController(){}



    // [Iter1 - Maor] – הצגת התפריט הראשי (Main Menu)
    public void showMainMenu(){
        SwingUtilities.invokeLater(() -> new view.MainMenuView(this).showSelf());
    }




}
