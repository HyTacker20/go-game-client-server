package com.example.goboard.controller.state;

import com.example.goboard.controller.GameController;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.MoveValidator;

/**
 * State representing active gameplay where players take turns.
 * This is the main state where most of the game happens.
 */
public class PlayingState extends AbstractGameState {

    public PlayingState(GameController controller, Board board, MoveValidator validator,
                        Player blackPlayer, Player whitePlayer, Player currentPlayer,
                        int consecutivePasses) {
        super(controller, board, validator, blackPlayer, whitePlayer, currentPlayer, consecutivePasses);
    }

    @Override
    public boolean play(int row, int col) {
        Stone stone = new Stone(currentPlayer.getColor());

        // Validate move first using strategy
        if (!validator.isValid(board, row, col, stone)) {
            return false;
        }

        // placeStone returns int → -1 means illegal move
        int result = board.placeStone(row, col, stone);
        boolean ok = (result != -1);

        if (ok) {
            // Reset consecutive passes after a successful move
            consecutivePasses = 0;
            swapPlayer();

            // Stay in playing state
            controller.setState(new PlayingState(controller, board, validator,
                    blackPlayer, whitePlayer, currentPlayer, consecutivePasses));
        }
        return ok;
    }

    @Override
    public boolean pass() {
        consecutivePasses++;

        if (consecutivePasses >= 2) {
            // Game ends after two consecutive passes
            controller.setState(new GameOverState(controller, board, validator,
                    blackPlayer, whitePlayer, currentPlayer, consecutivePasses));
            return true;
        } else {
            swapPlayer();
            // Stay in playing state with updated pass count
            controller.setState(new PlayingState(controller, board, validator,
                    blackPlayer, whitePlayer, currentPlayer, consecutivePasses));
            return false;
        }
    }

    @Override
    public boolean isGameOver() {
        return false;
    }
}
