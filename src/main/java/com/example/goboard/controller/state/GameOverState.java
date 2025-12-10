package com.example.goboard.controller.state;

import com.example.goboard.controller.GameController;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.strategy.MoveValidator;

/**
 * State representing a finished game.
 * No moves or passes are allowed in this state.
 */
public class GameOverState extends AbstractGameState {
    
    public GameOverState(GameController controller, Board board, MoveValidator validator,
                        Player blackPlayer, Player whitePlayer, Player currentPlayer,
                        int consecutivePasses) {
        super(controller, board, validator, blackPlayer, whitePlayer, currentPlayer, consecutivePasses);
    }
    
    @Override
    public boolean play(int row, int col) {
        // Cannot play moves when game is over
        return false;
    }
    
    @Override
    public boolean pass() {
        // Game is already over, return true
        return true;
    }
    
    @Override
    public boolean isGameOver() {
        return true;
    }
}
