package com.example.goboard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Go board.
 *
 * Responsibilities:
 * - stores board state (intersections and stones)
 * - validates and applies stone placement
 * - counts liberties (simplified: single stone only)
 * - removes stones without liberties
 *
 * NOTE:
 * This implementation supports ONLY single-stone capture.
 * Groups of connected stones are NOT handled yet.
 */
public class Board {

    /** Board size (e.g. 9, 13, 19). Fixed after construction. */
    private final int size;

    /** 2D grid of board intersections */
    private final Intersection[][] intersections;

    /** Observers notified about board changes */
    private final List<BoardListener> listeners = new ArrayList<>();

    /**
     * Creates a new Go board of given size.
     *
     * @param size board dimension (size x size)
     */
    public Board(int size) {
        if (size <= 0) throw new IllegalArgumentException("Size must be > 0");
        this.size = size;

        intersections = new Intersection[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                intersections[r][c] = new Intersection(r, c);
            }
        }
    }

    /**
     * @return board size
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the intersection at given coordinates.
     *
     * @param row row index
     * @param col column index
     * @return intersection or null if out of bounds
     */
    public Intersection getIntersection(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) return null;
        return intersections[row][col];
    }

    /** Registers a board listener (observer pattern). */
    public void addListener(BoardListener l) {
        listeners.add(l);
    }

    /** Removes a board listener. */
    public void removeListener(BoardListener l) {
        listeners.remove(l);
    }

    /**
     * Public method used by GameController.
     * Currently delegates to simplified placement logic.
     *
     * @return number of captured stones, or -1 if move is illegal
     */
    public int placeStone(int row, int col, Stone stone) {
        return placeSimple(row, col, stone);
    }

    /**
     * Simplified Go rules implementation:
     * - places a stone
     * - captures ONLY single adjacent enemy stones
     * - prevents suicide (single stone only)
     *
     * @return number of captured stones, or -1 if move is illegal
     */
    public int placeSimple(int row, int col, Stone stone) {
        Intersection it = getIntersection(row, col);
        if (it == null || !it.isEmpty()) return -1;

        // Place stone on the board
        it.setStone(stone);

        int captured = 0;

        // Directions: up, down, left, right
        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        // Check adjacent enemy stones for capture
        for (int[] d : dirs) {
            Intersection n = getIntersection(row + d[0], col + d[1]);
            if (n == null || n.isEmpty()) continue;

            if (n.getStone().getColor() != stone.getColor()) {
                if (removeSingleStoneIfDead(n.getRow(), n.getCol())) {
                    captured++;
                }
            }
        }

        // Suicide check (single stone only)
        if (countSingleStoneLiberties(row, col) == 0) {
            it.setStone(null);
            return -1;
        }

        return captured;
    }

    /**
     * Counts liberties (empty adjacent intersections)
     * for a SINGLE stone (no group logic).
     *
     * @return number of liberties
     */
    public int countSingleStoneLiberties(int row, int col) {
        Intersection it = getIntersection(row, col);
        if (it == null || it.isEmpty()) return 0;

        int liberties = 0;

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        for (int[] d : dirs) {
            Intersection n = getIntersection(row + d[0], col + d[1]);
            if (n != null && n.isEmpty()) {
                liberties++;
            }
        }

        return liberties;
    }

    /**
     * Removes a stone if it has no liberties.
     * Only works for single stones (no group handling).
     *
     * @return true if stone was removed
     */
    public boolean removeSingleStoneIfDead(int row, int col) {
        if (countSingleStoneLiberties(row, col) == 0) {
            Intersection it = getIntersection(row, col);
            if (it != null && !it.isEmpty()) {
                it.setStone(null);
                return true;
            }
        }
        return false;
    }
}
