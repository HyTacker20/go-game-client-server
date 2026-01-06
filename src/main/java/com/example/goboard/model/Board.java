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
                if (removeGroupIfDead(n.getRow(), n.getCol())) {
                    captured++;
                }
            }
        }

        // Suicide check (single stone only)
        if (countGroupLiberties(row, col) == 0) {
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
    public int countGroupLiberties(int row, int col) {
        Intersection start = getIntersection(row, col);
        if (start == null || start.isEmpty()) return 0;

        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();

        collectGroup(row, col, start.getStone().getColor(), visited, group);

        int liberties = 0;
        boolean[][] counted = new boolean[size][size];

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        for (Intersection it : group) {
            for (int[] d : dirs) {
                int r = it.getRow() + d[0];
                int c = it.getCol() + d[1];

                Intersection n = getIntersection(r, c);
                if (n != null && n.isEmpty() && !counted[r][c]) {
                    liberties++;
                    counted[r][c] = true; // nie liczymy tego samego oddechu 2 razy
                }
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
    public boolean removeGroupIfDead(int row, int col) {
        Intersection start = getIntersection(row, col);
        if (start == null || start.isEmpty()) return false;

        int liberties = countGroupLiberties(row, col);
        if (liberties > 0) return false;

        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();

        collectGroup(row, col, start.getStone().getColor(), visited, group);

        for (Intersection it : group) {
            it.setStone(null);
        }

        return true;
    }


    private void collectGroup(int row, int col, Stone.Color color, boolean[][] visited, List<Intersection> group) {
        Intersection it = getIntersection(row, col);
        if (it == null || it.isEmpty()) return;
        if (it.getStone().getColor() != color) return;
        if (visited[row][col]) return;

        visited[row][col] = true;
        group.add(it);

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        for (int[] d : dirs) {
            collectGroup(row + d[0], col + d[1], color, visited, group);
        }
    }

}