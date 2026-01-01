package model;

/**
 * Simple value object representing a player in the match
 */
public class Player {
    /** Player's display name */
    private final String name;

    /**
     * Creates a player with the given name
     */
    public Player(String name){
        this.name = name;
    }

    /** return player's name */
    public String name(){ return name; }
}
