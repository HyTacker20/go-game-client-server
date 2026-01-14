package com.example.goboard.model;

import java.util.*;

/**
 * Represents a Go board.
 *
 * Supports:
 * - stone placement
 * - captures
 * - group liberties
 * - Japanese scoring (territory + prisoners)
 *
 * Territory scoring:
 * Empty intersections are treated as stones of a third color.
 * Empty points form groups and have liberties just like stones.
 * If all liberties of an empty group belong to exactly one color,
 * the whole group counts as territory for that color.
 * If liberties belong to both colors, the group is neutral.
 */
public class Board {

    private final int size;
    private final Intersection[][] intersections;
    private Stone.Color[][] previousPosition;

    // Prisoners captured during the game
    private int blackPrisoners = 0;
    private int whitePrisoners = 0;

    // Komi: compensation points for white (typically 6.5 or 7.5)
    private final double komi;

    // Position history for superko detection
    private final List<String> positionHistory = new ArrayList<>();

    public Board(int size) {
        this(size, 6.5); // Default komi of 6.5
    }

    public Board(int size, double komi) {
        if (size <= 0) throw new IllegalArgumentException();
        this.size = size;
        this.komi = komi;

        intersections = new Intersection[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                intersections[r][c] = new Intersection(r, c);

        // Record initial empty board position
        positionHistory.add(boardToString());
    }

    /* ======================================================
       BASIC ACCESS
       ====================================================== */

    public int getSize() {
        return size;
    }

    public double getKomi() {
        return komi;
    }

    public Intersection getIntersection(int r, int c) {
        if (r < 0 || c < 0 || r >= size || c >= size) return null;
        return intersections[r][c];
    }

    /**
     * Get the number of white stones captured by black.
     */
    public int getBlackPrisoners() {
        return blackPrisoners;
    }

    /**
     * Get the number of black stones captured by white.
     */
    public int getWhitePrisoners() {
        return whitePrisoners;
    }

    /* ======================================================
       MOVE LOGIC
       ====================================================== */

    public int placeStone(int row, int col, Stone stone) {
        Intersection it = getIntersection(row, col);
        if (it == null || !it.isEmpty()) return -1;

        Stone.Color[][] before = copyBoardState();
        it.setStone(stone);

        int capturedThisMove = 0;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        for (int[] d : dirs) {
            Intersection n = getIntersection(row + d[0], col + d[1]);
            if (n != null && !n.isEmpty()
                    && n.getStone().getColor() != stone.getColor()) {

                int removed = removeGroupIfDeadAndCount(n.getRow(), n.getCol());
                if (removed > 0) {
                    capturedThisMove += removed;
                    if (stone.getColor() == Stone.Color.BLACK) {
                        blackPrisoners += removed;
                    } else {
                        whitePrisoners += removed;
                    }
                }
            }
        }

        // suicide
        if (countGroupLiberties(row, col) == 0 && capturedThisMove == 0) {
            it.setStone(null);
            return -1;
        }

        // superko - check against entire position history
        String currentPosition = boardToString();
        if (positionHistory.contains(currentPosition)) {
            restoreBoard(before);
            return -1;
        }

        // Valid move - add to history
        previousPosition = before;
        positionHistory.add(currentPosition);
        return capturedThisMove;
    }

    /* ======================================================
       GROUP & LIBERTIES
       ====================================================== */

    private int countGroupLiberties(int row, int col) {
        Intersection start = getIntersection(row, col);
        if (start == null || start.isEmpty()) return 0;

        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();
        collectGroup(row, col, start.getStone().getColor(), visited, group);

        Set<String> liberties = new HashSet<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        for (Intersection it : group) {
            for (int[] d : dirs) {
                int r = it.getRow() + d[0];
                int c = it.getCol() + d[1];
                Intersection n = getIntersection(r, c);
                if (n != null && n.isEmpty()) {
                    liberties.add(r + "," + c);
                }
            }
        }
        return liberties.size();
    }

    private int removeGroupIfDeadAndCount(int row, int col) {
        if (countGroupLiberties(row, col) > 0) return 0;

        Intersection start = getIntersection(row, col);
        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();
        collectGroup(row, col, start.getStone().getColor(), visited, group);

        for (Intersection it : group) {
            it.setStone(null);
        }
        return group.size();
    }

    private void collectGroup(int r, int c, Stone.Color color,
                              boolean[][] visited, List<Intersection> group) {
        Intersection it = getIntersection(r, c);
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

    /* ======================================================
       JAPANESE SCORING
       ====================================================== */

    /**
     * Prisoners = captured stones during the game
     * + marked dead stones of the opponent.
     */
    public int countStones(Stone.Color color) {
        int prisoners = (color == Stone.Color.BLACK)
                ? blackPrisoners
                : whitePrisoners;

        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                Intersection it = intersections[r][c];
                if (!it.isEmpty()
                        && it.isMarkedDead()
                        && it.getStone().getColor() != color) {
                    prisoners++;
                }
            }
        return prisoners;
    }

    /**
     * Territory counting based on empty-point groups.
     *
     * Empty intersections are treated like stones of a third color.
     * They form groups and have liberties.
     *
     * If all liberties of an empty group belong to exactly one color,
     * the entire group is territory of that color.
     *
     * If liberties belong to both colors, the group is neutral.
     *
     * Note: Seki regions are excluded from territory.
     */
    public int countTerritory(Stone.Color color) {
        boolean[][] visited = new boolean[size][size];
        int territory = 0;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {

                Intersection start = intersections[r][c];
                if (!start.isEmpty() || visited[r][c]) continue;

                // Skip seki regions
                if (start.isInSeki()) continue;

                List<Intersection> region = new ArrayList<>();
                Set<Stone.Color> liberties = new HashSet<>();

                collectEmptyGroup(r, c, visited, region, liberties);

                if (liberties.size() == 1 && liberties.contains(color)) {
                    territory += region.size();
                }
            }
        }
        return territory;
    }

    /**
     * Calculate final score for a color, including komi.
     * For white: territory - black_prisoners + komi
     * For black: territory - white_prisoners
     */
    public double calculateScore(Stone.Color color) {
        double score = countTerritory(color) - countStones(
            color == Stone.Color.BLACK ? Stone.Color.WHITE : Stone.Color.BLACK
        );

        // Add komi for white
        if (color == Stone.Color.WHITE) {
            score += komi;
        }

        return score;
    }

    /**
     * Flood fill for empty-point groups.
     *
     * Empty points are visited exactly once.
     * Stones are NOT marked as visited – they only define liberties.
     * Board edges do NOT count as liberties.
     */
    private void collectEmptyGroup(
            int r, int c,
            boolean[][] visited,
            List<Intersection> region,
            Set<Stone.Color> liberties) {

        if (r < 0 || c < 0 || r >= size || c >= size) return;

        Intersection it = intersections[r][c];

        // Stones define liberties but are not part of the region
        if (!it.isEmpty()) {
            liberties.add(it.getStone().getColor());
            return;
        }

        if (visited[r][c]) return;

        visited[r][c] = true;
        region.add(it);

        collectEmptyGroup(r+1, c, visited, region, liberties);
        collectEmptyGroup(r-1, c, visited, region, liberties);
        collectEmptyGroup(r, c+1, visited, region, liberties);
        collectEmptyGroup(r, c-1, visited, region, liberties);
    }

    /* ======================================================
       UTIL
       ====================================================== */

    private Stone.Color[][] copyBoardState() {
        Stone.Color[][] s = new Stone.Color[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                s[r][c] = intersections[r][c].isEmpty()
                        ? null
                        : intersections[r][c].getStone().getColor();
        return s;
    }

    private boolean isSameAsPrevious(Stone.Color[][] s) {
        if (previousPosition == null) return false;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (previousPosition[r][c] != s[r][c]) return false;
        return true;
    }

    private void restoreBoard(Stone.Color[][] s) {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                intersections[r][c].setStone(
                        s[r][c] == null ? null : new Stone(s[r][c]));
    }

    /**
     * Converts current board position to a string for superko detection.
     * Format: each intersection is represented by B (black), W (white), or . (empty)
     */
    private String boardToString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection it = intersections[r][c];
                if (it.isEmpty()) {
                    sb.append('.');
                } else if (it.getStone().getColor() == Stone.Color.BLACK) {
                    sb.append('B');
                } else {
                    sb.append('W');
                }
            }
        }
        return sb.toString();
    }

    /* ======================================================
       SEKI DETECTION
       ====================================================== */

    /**
     * Detect and mark seki regions on the board.
     *
     * Seki occurs when two groups share liberties and neither can capture
     * the other without being captured themselves.
     *
     * Algorithm:
     * 1. Find all groups with exactly 1 or 2 liberties
     * 2. Check if opposing groups share the same liberties
     * 3. If they share liberties and both would die if they play, it's seki
     */
    public void detectSeki() {
        // Clear previous seki marks
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                intersections[r][c].setInSeki(false);
            }
        }

        boolean[][] visited = new boolean[size][size];

        // Find all groups and check for seki
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (visited[r][c] || intersections[r][c].isEmpty()) continue;

                List<Intersection> group = new ArrayList<>();
                Stone.Color color = intersections[r][c].getStone().getColor();
                collectGroup(r, c, color, visited, group);

                int libertyCount = countGroupLiberties(r, c);

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
     * Check if a group is in seki by analyzing shared liberties with opponent.
     */
    private boolean isGroupInSeki(List<Intersection> group, Stone.Color color) {
        Set<Intersection> liberties = getGroupLiberties(group);
        if (liberties.isEmpty()) return false;

        Stone.Color opponentColor = (color == Stone.Color.BLACK) 
            ? Stone.Color.WHITE : Stone.Color.BLACK;

        // Check each liberty's neighbors for opponent groups
        for (Intersection liberty : liberties) {
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

            for (int[] d : dirs) {
                Intersection neighbor = getIntersection(
                    liberty.getRow() + d[0], 
                    liberty.getCol() + d[1]
                );

                if (neighbor != null && !neighbor.isEmpty() 
                    && neighbor.getStone().getColor() == opponentColor) {

                    // Found opponent group - check if it also has few liberties
                    int opponentLiberties = countGroupLiberties(
                        neighbor.getRow(), neighbor.getCol()
                    );

                    // Both groups have limited liberties and share space
                    if (opponentLiberties >= 1 && opponentLiberties <= 3) {
                        Set<Intersection> opponentLibs = getGroupLiberties(
                            getGroupAtPosition(neighbor.getRow(), neighbor.getCol())
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
     * Get all liberties of a group.
     */
    private Set<Intersection> getGroupLiberties(List<Intersection> group) {
        Set<Intersection> liberties = new HashSet<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        for (Intersection it : group) {
            for (int[] d : dirs) {
                Intersection neighbor = getIntersection(
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
     * Get group at a specific position.
     */
    private List<Intersection> getGroupAtPosition(int row, int col) {
        Intersection start = getIntersection(row, col);
        if (start == null || start.isEmpty()) return new ArrayList<>();

        boolean[][] visited = new boolean[size][size];
        List<Intersection> group = new ArrayList<>();
        collectGroup(row, col, start.getStone().getColor(), visited, group);
        return group;
    }

    /**
     * Mark all intersections in a group as being in seki.
     */
    private void markGroupAsSeki(List<Intersection> group) {
        for (Intersection it : group) {
            it.setInSeki(true);
        }

        // Also mark the shared liberties as seki
        Set<Intersection> liberties = getGroupLiberties(group);
        for (Intersection liberty : liberties) {
            liberty.setInSeki(true);
        }
    }
}
