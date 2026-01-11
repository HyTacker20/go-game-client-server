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

    public Board(int size) {
        if (size <= 0) throw new IllegalArgumentException();
        this.size = size;

        intersections = new Intersection[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                intersections[r][c] = new Intersection(r, c);
    }

    /* ======================================================
       BASIC ACCESS
       ====================================================== */

    public int getSize() {
        return size;
    }

    public Intersection getIntersection(int r, int c) {
        if (r < 0 || c < 0 || r >= size || c >= size) return null;
        return intersections[r][c];
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

        // ko
        Stone.Color[][] after = copyBoardState();
        if (isSameAsPrevious(after)) {
            restoreBoard(before);
            return -1;
        }

        previousPosition = before;
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
     */
    public int countTerritory(Stone.Color color) {
        boolean[][] visited = new boolean[size][size];
        int territory = 0;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {

                Intersection start = intersections[r][c];
                if (!start.isEmpty() || visited[r][c]) continue;

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
}
