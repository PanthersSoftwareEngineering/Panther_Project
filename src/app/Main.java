package app;
import controller.AppController;
import util.BackgroundMusic;

public class Main {
    public static void main(String[] args) {
        BackgroundMusic.start();
        AppController.getInstance().showMainMenu();
    }
}
