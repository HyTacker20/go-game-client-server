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

    /** Whether this intersection is part of a seki (mutual life) */
    private boolean inSeki;

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
        this.inSeki = false;
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

    /**
     * @return true if this intersection is in seki (mutual life)
     */
    public boolean isInSeki() {
        return inSeki;
    }

    /**
     * Marks or unmarks this intersection as being in seki.
     *
     * @param seki true if in seki, false otherwise
     */
    public void setInSeki(boolean seki) {
        this.inSeki = seki;
    }

    /* =====================================================
       REQUIRED FOR SCORING
       ===================================================== */

    /**
     * Returns the color of the stone if this intersection
     * is marked as dead.
     *
     * This method is REQUIRED by the scoring logic.
     *
     * @return color of dead stone, or null if:
     *         - no stone
     *         - stone is not marked dead
     */
    public Stone.Color getMarkedDeadColor() {
        if (stone == null || !markedDead) {
            return null;
        }
        return stone.getColor();
    }
}
