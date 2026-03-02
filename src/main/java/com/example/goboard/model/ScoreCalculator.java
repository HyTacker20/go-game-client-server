package com.example.goboard.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calculates scores and territory for Japanese Go rules.
 * 
 * Responsibilities:
 * - Count territory based on empty regions
 * - Count prisoners (captured stones + marked dead stones)
 * - Calculate final scores
 */
public class ScoreCalculator {
    
    private static final int[][] DIRECTIONS = {{1,0},{-1,0},{0,1},{0,-1}};
    
    private final Board board;
    private final int size;
    
    public ScoreCalculator(Board board) {
        this.board = board;
        this.size = board.getSize();
    }
    
    /**
     * Count prisoners for a color.
     * Includes: captured stones during game + marked dead stones of opponent.
     * 
     * @param color the color to count prisoners for
     * @param blackPrisoners captured white stones (for black)
     * @param whitePrisoners captured black stones (for white)
     * @return total prisoner count
     */
    public int countPrisoners(Stone.Color color, int blackPrisoners, int whitePrisoners) {
        int prisoners = (color == Stone.Color.BLACK)
                ? blackPrisoners
                : whitePrisoners;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection it = board.getIntersection(r, c);
                if (!it.isEmpty()
                        && it.isMarkedDead()
                        && it.getStone().getColor() != color) {
                    prisoners++;
                }
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
     * 
     * @param color the color to count territory for
     * @return territory count
     */
    public int countTerritory(Stone.Color color) {
        boolean[][] visited = new boolean[size][size];
        int territory = 0;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {

                Intersection start = board.getIntersection(r, c);
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
     * Calculate final score for a color.
     * Formula: territory + captured_opponent_stones
     * 
     * @param color the color to calculate score for
     * @param blackPrisoners captured white stones (won by black)
     * @param whitePrisoners captured black stones (won by white)
     * @return final score
     */
    public double calculateScore(Stone.Color color, int blackPrisoners, int whitePrisoners) {
        int territory = countTerritory(color);
        
        if (color == Stone.Color.BLACK) {
            // Black score = black territory + white stones captured by black
            return territory + blackPrisoners;
        } else {
            // White score = white territory + black stones captured by white
            return territory + whitePrisoners;
        }
    }
    
    /**
     * Flood fill for empty-point groups.
     *
     * Empty points are visited exactly once.
     * Stones are NOT marked as visited – they only define liberties.
     *
     * @param r starting row
     * @param c starting column
     * @param visited tracking array
     * @param region output list of empty intersections
     * @param liberties output set of colors touching this empty region
     */
    private void collectEmptyGroup(
            int r, int c,
            boolean[][] visited,
            List<Intersection> region,
            Set<Stone.Color> liberties) {

        Intersection it = board.getIntersection(r, c);
        if (it == null || visited[r][c]) return;

        if (!it.isEmpty()) {
            // Hit a stone → record its color as a liberty
            liberties.add(it.getStone().getColor());
            return;
        }

        visited[r][c] = true;
        region.add(it);

        collectEmptyGroup(r+1, c, visited, region, liberties);
        collectEmptyGroup(r-1, c, visited, region, liberties);
        collectEmptyGroup(r, c+1, visited, region, liberties);
        collectEmptyGroup(r, c-1, visited, region, liberties);
    }
}
