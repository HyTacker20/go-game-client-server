package com.example.goboard.controller;

import com.example.goboard.controller.state.GameState;
import com.example.goboard.controller.state.PlayingState;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.MoveValidator;

/**
 * GameController is the central coordinator of the Go game.
 *
 * It uses the State pattern to delegate game behavior
 * (playing, passing, game over) to dedicated state objects.
 *
 * Thanks to this approach:
 * - game logic is separated from state management
 * - adding new game phases is easy (e.g. scoring phase)
 * - conditional logic (if/else) is minimized
 */
public class GameController {

    /** Game board model */
    private final Board board;

    /** Strategy responsible for validating moves */
    private final MoveValidator validator;

    /** Black player instance */
    private final Player blackPlayer;

    /** White player instance */
    private final Player whitePlayer;

    /**
     * Current game state.
     * All actions (play, pass, game over checks) are delegated to this object.
     */
    private GameState currentState;

    /**
     * Full constructor allowing explicit configuration of both players
     * and the starting player.
     *
     * This constructor is useful for network games or advanced setups.
     */
    public GameController(Board board,
                          MoveValidator validator,
                          Player black,
                          Player white,
                          Player starting) {

        this.board = board;
        this.validator = validator;
        this.blackPlayer = black;
        this.whitePlayer = white;

        // Determine which player starts the game
        Player startingPlayer = starting != null ? starting : black;

        // Initial state of the game is always PlayingState
        this.currentState = new PlayingState(
                this,
                board,
                validator,
                blackPlayer,
                whitePlayer,
                startingPlayer,
                0
        );
    }

    /**
     * Simplified constructor for local games.
     *
     * Only one player needs to be provided;
     * the second one is created automatically with the opposite color.
     */
    public GameController(Board board, MoveValidator validator, Player starting) {
        this.board = board;
        this.validator = validator;

        // Assign players based on the starting player's color
        this.blackPlayer = starting.getColor() == Stone.Color.BLACK
                ? starting
                : new Player("Black", Stone.Color.BLACK);

        this.whitePlayer = starting.getColor() == Stone.Color.WHITE
                ? starting
                : new Player("White", Stone.Color.WHITE);

        // Game starts in PlayingState
        this.currentState = new PlayingState(
                this,
                board,
                validator,
                blackPlayer,
                whitePlayer,
                starting,
                0
        );
    }

    /**
     * Attempts to place a stone on the board.
     *
     * The actual behavior depends on the current game state.
     *
     * @return true if the move was legal and executed
     */
    public boolean play(int row, int col) {
        return currentState.play(row, col);
    }

    /**
     * Current player passes their turn.
     *
     * Two consecutive passes typically end the game.
     *
     * @return true if the game ended as a result of this pass
     */
    public boolean pass() {
        return currentState.pass();
    }

    /**
     * Returns the player whose turn it currently is.
     */
    public Player getCurrentPlayer() {
        return currentState.getCurrentPlayer();
    }

    /**
     * Indicates whether the game has ended.
     */
    public boolean isGameOver() {
        return currentState.isGameOver();
    }

    /**
     * Returns the number of consecutive passes.
     * This information is maintained by the active state.
     */
    public int getConsecutivePasses() {
        return currentState.getConsecutivePasses();
    }

    /**
     * Changes the current game state.
     *
     * This method is called internally by state objects
     * to transition between phases (e.g. Playing → GameOver).
     */
    public void setState(GameState state) {
        this.currentState = state;
    }
}
