package com.example.goboard.controller;

import com.example.goboard.controller.state.GameState;
import com.example.goboard.controller.state.PlayingState;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.MoveValidator;

/**
 * Game controller using State pattern to manage different game phases.
 * Delegates behavior to current state object, making the code more maintainable
 * and easier to extend with new game phases.
 */
public class GameController {
    private final Board board;
    private final MoveValidator validator;
    private final Player blackPlayer;
    private final Player whitePlayer;
    
    private GameState currentState;

    public GameController(Board board, MoveValidator validator, Player black, Player white, Player starting) {
        this.board = board;
        this.validator = validator;
        this.blackPlayer = black;
        this.whitePlayer = white;
        
        Player startingPlayer = starting != null ? starting : black;
        // Initialize in playing state
        this.currentState = new PlayingState(this, board, validator, 
            blackPlayer, whitePlayer, startingPlayer, 0);
    }

    /**
     * Alternative constructor for simpler calls: provide only one starting player.
     * The second player is automatically created with opposite color.
     */
    public GameController(Board board, MoveValidator validator, Player starting) {
        this.board = board;
        this.validator = validator;
        this.blackPlayer = starting.getColor() == Stone.Color.BLACK ? 
            starting : new Player("Black", Stone.Color.BLACK);
        this.whitePlayer = starting.getColor() == Stone.Color.WHITE ? 
            starting : new Player("White", Stone.Color.WHITE);
        
        // Initialize in playing state
        this.currentState = new PlayingState(this, board, validator,
            blackPlayer, whitePlayer, starting, 0);
    }

    /**
     * Execute a move (place stone). Returns true when move is valid and executed.
     * Delegates to current state.
     */
    public boolean play(int row, int col) {
        return currentState.play(row, col);
    }

    /**
     * Player passes. Returns true if game ended (e.g., after two passes).
     * Delegates to current state.
     */
    public boolean pass() {
        return currentState.pass();
    }

    /**
     * Get the current player for this turn.
     * Delegates to current state.
     */
    public Player getCurrentPlayer() {
        return currentState.getCurrentPlayer();
    }

    /**
     * Check if game is over.
     * Delegates to current state.
     */
    public boolean isGameOver() {
        return currentState.isGameOver();
    }

    /**
     * Get the number of consecutive passes.
     * Delegates to current state.
     */
    public int getConsecutivePasses() {
        return currentState.getConsecutivePasses();
    }
    
    /**
     * Set the current game state.
     * Public for use by state implementations.
     */
    public void setState(GameState state) {
        this.currentState = state;
    }
}
