package com.example.goboard.controller.state;

import com.example.goboard.controller.GameController;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.strategy.MoveValidator;

/**
 * Base class for game states, providing common functionality.
 * Uses Template Method pattern for shared behavior.
 */
public abstract class AbstractGameState implements GameState {
    protected final GameController controller;
    protected final Board board;
    protected final MoveValidator validator;
    protected final Player blackPlayer;
    protected final Player whitePlayer;
    protected Player currentPlayer;
    protected int consecutivePasses;
    
    public AbstractGameState(GameController controller, Board board, MoveValidator validator,
                            Player blackPlayer, Player whitePlayer, Player currentPlayer,
                            int consecutivePasses) {
        this.controller = controller;
        this.board = board;
        this.validator = validator;
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.currentPlayer = currentPlayer;
        this.consecutivePasses = consecutivePasses;
    }
    
    protected void swapPlayer() {
        if (currentPlayer == blackPlayer) {
            currentPlayer = whitePlayer;
        } else {
            currentPlayer = blackPlayer;
        }
    }
    
    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    @Override
    public int getConsecutivePasses() {
        return consecutivePasses;
    }
}
