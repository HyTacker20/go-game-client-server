package com.example.goboard.model;

/**
 * Represents a single intersection (point) on the Go board.
 *
 * Each intersection:
 * - has fixed coordinates (row, column)
 * - can contain at most one stone
 *
 * The intersection itself does NOT know game rules.
 * All rule logic is handled by the Board class.
 */
public class Intersection {

    /** Row index of the intersection on the board */
    private final int row;

    /** Column index of the intersection on the board */
    private final int col;

    /** Stone currently placed on this intersection (null if empty) */
    private Stone stone;

    /**
     * Creates a new intersection at given coordinates.
     *
     * @param row row index
     * @param col column index
     */
    public Intersection(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /** @return row index */
    public int getRow() {
        return row;
    }

    /** @return column index */
    public int getCol() {
        return col;
    }

    /** @return true if no stone is placed on this intersection */
    public boolean isEmpty() {
        return stone == null;
    }

    /** @return stone placed on this intersection, or null if empty */
    public Stone getStone() {
        return stone;
    }

    /**
     * Places or removes a stone on this intersection.
     * Passing null removes the stone.
     *
     * @param s stone to place, or null to clear
     */
    public void setStone(Stone s) {
        this.stone = s;
    }
}
