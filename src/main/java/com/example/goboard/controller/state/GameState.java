package com.example.goboard.controller.state;

import com.example.goboard.model.Player;

/**
 * State pattern interface for managing different game phases.
 * Each state encapsulates the behavior for that particular game phase.
 */
public interface GameState {
    
    /**
     * Attempt to play a move in this state.
     * 
     * @param row Row coordinate
     * @param col Column coordinate
     * @return true if move was valid and executed
     */
    boolean play(int row, int col);
    
    /**
     * Attempt to pass in this state.
     * 
     * @return true if game ended after this pass
     */
    boolean pass();
    
    /**
     * Get the current player for this turn.
     * 
     * @return Current player
     */
    Player getCurrentPlayer();
    
    /**
     * Check if the game is over in this state.
     * 
     * @return true if game is over
     */
    boolean isGameOver();
    
    /**
     * Get the number of consecutive passes.
     * 
     * @return consecutive pass count
     */
    int getConsecutivePasses();
}
