package model;

/**
 * Observer interface for Match updates.
 * View implements this to get automatic UI updates.
 */
public interface MatchListener {
    void onMatchChanged(MatchSnapshot s);
}
