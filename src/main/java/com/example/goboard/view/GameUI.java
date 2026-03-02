package com.example.goboard.view;

import com.example.goboard.model.Board;

/**
 * Abstraction layer for all UI implementations (Console, GUI, etc.).
 *
 * The GameUI interface defines every interaction between the user
 * and the game, while keeping all game logic outside the view layer.
 *
 * This includes:
 * - normal gameplay input/output
 * - scoring phase interactions
 * - score confirmation
 */
public interface GameUI {

    // =========================
    // GENERAL DISPLAY
    // =========================

    /**
     * Display the current board state.
     * Used during normal gameplay.
     */
    void displayBoard(Board board);

    /**
     * Display an informational message to the user.
     */
    void displayMessage(String message);

    // =========================
    // GAMEPLAY INPUT
    // =========================

    /**
     * Request a move input from the current player.
     *
     * Expected commands:
     * - board coordinates (e.g. "D4")
     * - "pass"
     * - "quit"
     */
    String getMoveInput(String prompt);

    /**
     * Request arbitrary string input from the user.
     */
    String getStringInput(String prompt);

    /**
     * Request integer input with bounds checking.
     */
    int getIntegerInput(String prompt, int min, int max);

    // =========================
    // SCORING PHASE
    // =========================

    /**
     * Display the board during the scoring phase.
     *
     * Dead groups should be visually distinguished
     * (e.g. marked, dimmed, crossed out, etc.).
     */
    void displayScoringBoard(Board board);

    /**
     * Ask the player to select a stone belonging to a group
     * that should be marked or unmarked as dead.
     *
     * @param prompt message shown to the player
     * @return int[2] = {row, col} or null if the player finishes marking
     */
    int[] getDeadGroupSelection(String prompt);

    /**
     * Display the currently calculated score.
     */
    void displayScore(double blackScore, double whiteScore);

    /**
     * Ask the player to confirm the final score.
     *
     * @param playerName name of the player being asked
     * @return true if the score is accepted,
     *         false if the player requests to resume the game
     */
    boolean confirmScore(String playerName);

    // =========================
    // CLEANUP
    // =========================

    /**
     * Close and clean up UI resources
     * (e.g. Scanner, windows, streams).
     */
    void close();
}
