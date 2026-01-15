package com.example.goboard.model;

import java.util.*;

/**
 * Detects and marks seki (mutual life) situations on the Go board.
 * 
 * Seki occurs when two groups share liberties and neither can capture
 * the other without being captured themselves. Stones in seki are alive
 * but do not count as territory in Japanese rules.
 * 
 * Responsibilities:
 * - Detect seki patterns
 * - Mark intersections as being in seki
 * - Clear seki markings
 */
public class SekiDetector {
    
    private static final int[][] DIRECTIONS = {{1,0},{-1,0},{0,1},{0,-1}};
    
    private final Board board;
    private final CaptureEngine captureEngine;
    private final int size;
    
    public SekiDetector(Board board, CaptureEngine captureEngine) {
        this.board = board;
        this.captureEngine = captureEngine;
        this.size = board.getSize();
    }
    
    /**
     * Detect and mark seki regions on the board.
     *
     * Algorithm:
     * 1. Find all groups with exactly 1-3 liberties
     * 2. Check if opposing groups share the same liberties
     * 3. If they share liberties and both would die if they play, it's seki
     */
    public void detectAndMarkSeki() {
        // Clear previous seki marks
        clearSekiMarks();

        boolean[][] visited = new boolean[size][size];

        // Find all groups and check for seki
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection it = board.getIntersection(r, c);
                if (visited[r][c] || it.isEmpty()) continue;

                List<Intersection> group = new ArrayList<>();
                Stone.Color color = it.getStone().getColor();
                captureEngine.collectGroup(r, c, color, visited, group);

                int libertyCount = captureEngine.countGroupLiberties(r, c);

                // Potential seki: group has 1-3 liberties
                if (libertyCount >= 1 && libertyCount <= 3) {
                    if (isGroupInSeki(group, color)) {
                        markGroupAsSeki(group);
                    }
                }
            }
        }
    }
    
    /**
     * Clear all seki markings on the board.
     */
    public void clearSekiMarks() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                board.getIntersection(r, c).setInSeki(false);
            }
        }
    }
    
    /**
     * Check if a group is in seki by analyzing shared liberties with opponent.
     * 
     * @param group the group to check
     * @param color the color of the group
     * @return true if the group is in seki
     */
    private boolean isGroupInSeki(List<Intersection> group, Stone.Color color) {
        Set<Intersection> liberties = captureEngine.getGroupLiberties(group);
        if (liberties.isEmpty()) return false;

        Stone.Color opponentColor = (color == Stone.Color.BLACK) 
            ? Stone.Color.WHITE : Stone.Color.BLACK;

        // Check each liberty's neighbors for opponent groups
        for (Intersection liberty : liberties) {
            for (int[] d : DIRECTIONS) {
                Intersection neighbor = board.getIntersection(
                    liberty.getRow() + d[0], 
                    liberty.getCol() + d[1]
                );

                if (neighbor != null && !neighbor.isEmpty() 
                    && neighbor.getStone().getColor() == opponentColor) {

                    // Found opponent group - check if it also has few liberties
                    int opponentLiberties = captureEngine.countGroupLiberties(
                        neighbor.getRow(), neighbor.getCol()
                    );

                    // Both groups have limited liberties and share space
                    if (opponentLiberties >= 1 && opponentLiberties <= 3) {
                        Set<Intersection> opponentLibs = captureEngine.getGroupLiberties(
                            captureEngine.getGroupAtPosition(neighbor.getRow(), neighbor.getCol())
                        );

                        // Check if they share liberties
                        Set<Intersection> shared = new HashSet<>(liberties);
                        shared.retainAll(opponentLibs);

                        if (!shared.isEmpty()) {
                            return true; // Likely seki
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Mark all intersections in a group as being in seki.
     * Also marks the shared liberties as seki.
     * 
     * @param group the group to mark
     */
    private void markGroupAsSeki(List<Intersection> group) {
        for (Intersection it : group) {
            it.setInSeki(true);
        }

        // Also mark the shared liberties as seki
        Set<Intersection> liberties = captureEngine.getGroupLiberties(group);
        for (Intersection liberty : liberties) {
            liberty.setInSeki(true);
        }
    }
}
