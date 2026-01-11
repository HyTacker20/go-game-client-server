package com.example.goboard.model;

/**
 * Represents a single intersection (point) on the Go board.
 *
 * Responsibilities:
 * - Stores row and column coordinates
 * - Can hold a single stone (or be empty)
 * - Can be marked as "dead" for scoring purposes
 *
 * Game rules (liberties, captures, etc.) are handled by the Board class.
 */
public class Intersection {

    /** Row index of the intersection on the board */
    private final int row;

    /** Column index of the intersection on the board */
    private final int col;

    /** Stone placed on this intersection (null if empty) */
    private Stone stone;

    /** Whether this intersection is marked as dead during scoring */
    private boolean markedDead;

    /**
     * Creates a new intersection at given coordinates.
     *
     * @param row row index
     * @param col column index
     */
    public Intersection(int row, int col) {
        this.row = row;
        this.col = col;
        this.stone = null;
        this.markedDead = false;
    }

    /** @return row index of this intersection */
    public int getRow() {
        return row;
    }

    /** @return column index of this intersection */
    public int getCol() {
        return col;
    }

    /** @return true if no stone is placed here */
    public boolean isEmpty() {
        return stone == null;
    }

    /** @return the stone on this intersection (null if empty) */
    public Stone getStone() {
        return stone;
    }

    /**
     * Places or removes a stone on this intersection.
     * Passing null removes the stone.
     *
     * @param s Stone to place, or null to remove
     */
    public void setStone(Stone s) {
        this.stone = s;
    }

    /** @return true if this intersection is marked as dead */
    public boolean isMarkedDead() {
        return markedDead;
    }

    /**
     * Marks or unmarks this intersection as dead for scoring.
     *
     * @param dead true to mark as dead, false to unmark
     */
    public void setMarkedDead(boolean dead) {
        this.markedDead = dead;
    }

    /**
     * Toggles the dead marker.
     * Useful for console marking: click once to mark, click again to unmark.
     */
    public void toggleDeadMarker() {
        this.markedDead = !this.markedDead;
    }
}
