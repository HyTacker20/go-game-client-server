package com.example.goboard.model;

import java.util.*;

/**
 * Handles capture logic, liberty counting, and group collection for a Go board.
 * 
 * Responsibilities:
 * - Count liberties for stone groups
 * - Collect connected groups of stones
 * - Remove captured groups
 * - Determine if a group is dead (zero liberties)
 */
public class CaptureEngine {
    
    private static final int[][] DIRECTIONS = {{1,0},{-1,0},{0,1},{0,-1}};
    
    private final Board board;
    private final int size;
    
    public CaptureEngine(Board board) {
        this.board = board;
        this.size = board.getSize();
    }
    
    /**
     * Count the number of liberties (empty adjacent points) for the group
     * containing the stone at the given position.
     * 
     * @param row starting row
     * @param col starting column
     * @return number of liberties for the group
     */
    public int countGroupLiberties(int row, int col) {
        Intersection start = board.getIntersection(row, col);
        if (start == null || start.isEmpty()) return 0;

        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();
        collectGroup(row, col, start.getStone().getColor(), visited, group);

        Set<String> liberties = new HashSet<>();

        for (Intersection it : group) {
            for (int[] d : DIRECTIONS) {
                int r = it.getRow() + d[0];
                int c = it.getCol() + d[1];
                Intersection n = board.getIntersection(r, c);
                if (n != null && n.isEmpty()) {
                    liberties.add(r + "," + c);
                }
            }
        }
        return liberties.size();
    }
    
    /**
     * Remove a group if it has no liberties and return the count of removed stones.
     * 
     * @param row starting row
     * @param col starting column
     * @return number of stones removed, or 0 if the group is alive
     */
    public int removeGroupIfDeadAndCount(int row, int col) {
        if (countGroupLiberties(row, col) > 0) return 0;

        Intersection start = board.getIntersection(row, col);
        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();
        collectGroup(row, col, start.getStone().getColor(), visited, group);

        for (Intersection it : group) {
            it.setStone(null);
        }
        return group.size();
    }
    
    /**
     * Collect all stones in a connected group starting from the given position.
     * Uses flood-fill algorithm to find all connected stones of the same color.
     * 
     * @param r starting row
     * @param c starting column
     * @param color stone color to match
     * @param visited tracking array to prevent infinite recursion
     * @param group output list to collect group members
     */
    public void collectGroup(int r, int c, Stone.Color color,
                              boolean[][] visited, List<Intersection> group) {
        Intersection it = board.getIntersection(r, c);
        if (it == null || it.isEmpty()
                || visited[r][c]
                || it.getStone().getColor() != color) return;

        visited[r][c] = true;
        group.add(it);

        collectGroup(r+1, c, color, visited, group);
        collectGroup(r-1, c, color, visited, group);
        collectGroup(r, c+1, color, visited, group);
        collectGroup(r, c-1, color, visited, group);
    }
    
    /**
     * Get all intersections in the group at a specific position.
     * 
     * @param row row coordinate
     * @param col column coordinate
     * @return list of intersections in the group, empty list if invalid position
     */
    public List<Intersection> getGroupAtPosition(int row, int col) {
        Intersection start = board.getIntersection(row, col);
        if (start == null || start.isEmpty()) return new ArrayList<>();

        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();
        collectGroup(row, col, start.getStone().getColor(), visited, group);
        return group;
    }
    
    /**
     * Get all liberties (empty adjacent intersections) for a group.
     * 
     * @param group list of intersections in the group
     * @return set of empty intersections adjacent to the group
     */
    public Set<Intersection> getGroupLiberties(List<Intersection> group) {
        Set<Intersection> liberties = new HashSet<>();

        for (Intersection it : group) {
            for (int[] d : DIRECTIONS) {
                Intersection neighbor = board.getIntersection(
                    it.getRow() + d[0], it.getCol() + d[1]
                );
                if (neighbor != null && neighbor.isEmpty()) {
                    liberties.add(neighbor);
                }
            }
        }
        return liberties;
    }
    
    /**
     * Process captures after a stone is placed.
     * Checks all adjacent enemy groups and removes those with zero liberties.
     * 
     * @param row row where stone was placed
     * @param col column where stone was placed
     * @param stoneColor color of the placed stone
     * @return number of stones captured
     */
    public int processCapturesAfterMove(int row, int col, Stone.Color stoneColor) {
        int capturedThisMove = 0;

        for (int[] d : DIRECTIONS) {
            Intersection n = board.getIntersection(row + d[0], col + d[1]);
            if (n != null && !n.isEmpty()
                    && n.getStone().getColor() != stoneColor) {

                int removed = removeGroupIfDeadAndCount(n.getRow(), n.getCol());
                capturedThisMove += removed;
            }
        }
        
        return capturedThisMove;
    }
}
