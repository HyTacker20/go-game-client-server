package com.example.goboard.view;

import com.example.goboard.model.Board;

/**
 * Abstraction layer for UI implementations (Console, GUI, etc.)
 * This allows easy switching between different UI implementations without changing game logic.
 */
public interface GameUI {
    
    /**
     * Display the current board state to the user.
     */
    void displayBoard(Board board);
    
    /**
     * Request a move input from the player.
     * Should return a string representing the move (e.g., "D4", "pass", "quit").
     */
    String getMoveInput(String prompt);
    
    /**
     * Display a message to the user.
     */
    void displayMessage(String message);
    
    /**
     * Request string input from the user.
     */
    String getStringInput(String prompt);
    
    /**
     * Request integer input from the user.
     */
    int getIntegerInput(String prompt, int min, int max);
    
    /**
     * Close/cleanup the UI (close scanners, windows, etc.)
     */
    void close();
}
