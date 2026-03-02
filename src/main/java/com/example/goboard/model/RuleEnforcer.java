package com.example.goboard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Enforces Go game rules: ko, superko, and suicide.
 * 
 * Responsibilities:
 * - Track position history for superko detection
 * - Check for ko (simple repetition)
 * - Validate suicide moves
 * - Manage board state snapshots
 */
public class RuleEnforcer {
    
    private final Board board;
    private final int size;
    
    // Position history for superko detection
    private final List<String> positionHistory = new ArrayList<>();
    private Stone.Color[][] previousPosition;
    
    public RuleEnforcer(Board board) {
        this.board = board;
        this.size = board.getSize();
        
        // Record initial empty board position
        positionHistory.add(boardToString());
    }
    
    /**
     * Check if a position would violate superko rule.
     * Superko means the board position has occurred before in the game.
     * 
     * @param boardState current board state after the move
     * @return true if this position has occurred before
     */
    public boolean violatesSuperko(Stone.Color[][] boardState) {
        String currentPosition = boardStateToString(boardState);
        return positionHistory.contains(currentPosition);
    }
    
    /**
     * Check if a position is the same as the previous position (simple ko).
     * 
     * @param boardState current board state
     * @return true if this is the same as the previous board position
     */
    public boolean isSameAsPrevious(Stone.Color[][] boardState) {
        if (previousPosition == null) return false;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (previousPosition[r][c] != boardState[r][c]) return false;
            }
        }
        return true;
    }
    
    /**
     * Record a valid move position in history.
     * 
     * @param boardState the board state to record
     */
    public void recordPosition(Stone.Color[][] boardState) {
        previousPosition = copyBoardState(boardState);
        positionHistory.add(boardStateToString(boardState));
    }
    
    /**
     * Get a snapshot of current board state.
     * 
     * @return 2D array of stone colors (null for empty)
     */
    public Stone.Color[][] snapshotBoardState() {
        Stone.Color[][] state = new Stone.Color[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection it = board.getIntersection(r, c);
                state[r][c] = it.isEmpty() ? null : it.getStone().getColor();
            }
        }
        return state;
    }
    
    /**
     * Restore board to a previous state.
     * 
     * @param state the board state to restore
     */
    public void restoreBoardState(Stone.Color[][] state) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection it = board.getIntersection(r, c);
                it.setStone(state[r][c] == null ? null : new Stone(state[r][c]));
            }
        }
    }
    
    /**
     * Converts current board position to a string for comparison.
     * Format: each intersection is represented by B (black), W (white), or . (empty)
     */
    private String boardToString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection it = board.getIntersection(r, c);
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
    
    /**
     * Converts a board state array to string format.
     */
    private String boardStateToString(Stone.Color[][] state) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (state[r][c] == null) {
                    sb.append('.');
                } else if (state[r][c] == Stone.Color.BLACK) {
                    sb.append('B');
                } else {
                    sb.append('W');
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * Create a deep copy of board state array.
     */
    private Stone.Color[][] copyBoardState(Stone.Color[][] state) {
        Stone.Color[][] copy = new Stone.Color[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                copy[r][c] = state[r][c];
            }
        }
        return copy;
    }
}
