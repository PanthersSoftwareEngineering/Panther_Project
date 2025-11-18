package app;
import controller.AppController;

public class Main {

    // [Iter1 - Maor] – נקודת כניסה שמציגה את התפריט הראשי
    public static void main(String[] args) {
        AppController.getInstance().showMainMenu();
    }
}
