package view;

/**
 * Callback used by the 'new match' screen
 * When the user presses "Start", the view calls onStart(...)
 * and the AppController (which implements this) starts the game
 */
public interface StartListener {
    void onStart(String p1, String p2, String diff);
}
