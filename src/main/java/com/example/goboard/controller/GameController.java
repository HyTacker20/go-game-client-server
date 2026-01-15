package com.example.goboard.controller;

import com.example.goboard.controller.state.GameState;
import com.example.goboard.controller.state.PlayingState;
import com.example.goboard.controller.state.ScoringState;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.MoveValidator;
import com.example.goboard.view.GameUI;

/**
 * GameController is the central coordinator of the Go game.
 *
 * Responsibilities:
 * - Holds references to core game components (Board, Players, MoveValidator)
 * - Delegates game actions to the current GameState
 * - Manages state transitions (Playing → Scoring → GameOver)
 */
public class GameController {

    private final Board board;
    private final MoveValidator validator;
    private final Player blackPlayer;
    private final Player whitePlayer;

    private GameState currentState;

    /**
     * Constructor with explicit black and white players.
     * Always starts with black as the first player.
     */
    public GameController(Board board,
                          MoveValidator validator,
                          Player black,
                          Player white) {
        this.board = board;
        this.validator = validator;
        this.blackPlayer = black;
        this.whitePlayer = white;

        // Starting player is black by default
        Player startingPlayer = black;

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
     * Creates the missing player automatically.
     */
    public GameController(Board board, MoveValidator validator, Player starting) {
        this.board = board;
        this.validator = validator;

        if (starting.getColor() == Stone.Color.BLACK) {
            this.blackPlayer = starting;
            this.whitePlayer = Player.defaultWhite();
        } else {
            this.whitePlayer = starting;
            this.blackPlayer = Player.defaultBlack();
        }

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

    /** Attempts to play a move via the current GameState */
    public boolean play(int row, int col) {
        return currentState.play(row, col);
    }

    /** Current player passes their turn */
    public boolean pass() {
        return currentState.pass();
    }

    /** Returns the player whose turn it currently is */
    public Player getCurrentPlayer() {
        return currentState.getCurrentPlayer();
    }

    /** Returns true if the game is over */
    public boolean isGameOver() {
        return currentState.isGameOver();
    }

    /** Returns the number of consecutive passes */
    public int getConsecutivePasses() {
        return currentState.getConsecutivePasses();
    }

    /** Sets the current game state (used by state objects) */
    public void setState(GameState state) {
        this.currentState = state;
    }

    /** Returns the current game state (for UI or logic) */
    public GameState getState() {
        return currentState;
    }

    /** Returns true if the game is currently in scoring phase */
    public boolean isScoringPhase() {
        return currentState instanceof ScoringState;
    }

    /** Delegates scoring handling to the ScoringState */
    public void handleScoring(GameUI ui) {
        if (currentState instanceof ScoringState scoringState) {
            scoringState.handleScoring(ui);
        }
    }

    /** Getters for players and board */
    public Player getBlackPlayer() { return blackPlayer; }
    public Player getWhitePlayer() { return whitePlayer; }
    public Board getBoard() { return board; }
}
