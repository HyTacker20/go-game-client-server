package com.example.goboard.model;

/**
 * Represents a player in the Go game.
 *
 * A player:
 * - has a display name
 * - is assigned exactly one stone color (BLACK or WHITE)
 *
 * The Player class contains no game logic.
 * It is a simple data holder used by the controller and game states.
 */
public class Player {

    /** Player display name (e.g. "Black", "White", or custom name) */
    private final String name;

    /** Stone color assigned to this player */
    private final Stone.Color color;

    /**
     * Creates a new player.
     *
     * @param name  player name
     * @param color stone color assigned to the player
     */
    public Player(String name, Stone.Color color) {
        this.name = name;
        this.color = color;
    }

    /** @return player's display name */
    public String getName() {
        return name;
    }

    /** @return stone color assigned to the player */
    public Stone.Color getColor() {
        return color;
    }
}
