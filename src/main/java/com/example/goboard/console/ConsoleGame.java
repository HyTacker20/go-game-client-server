package com.example.goboard.console;

import com.example.goboard.controller.GameController;
import com.example.goboard.factory.BoardFactory;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.SimpleMoveValidator;
import com.example.goboard.view.GameUI;
import com.example.goboard.view.ConsoleUIFormatter;

/**
 * ConsoleGame represents a local (offline) Go game played in the console.
 *
 * This class acts as a composition root for the local game:
 * - creates the board using a factory
 * - initializes the game controller
 * - connects user input/output via GameUI
 *
 * Thanks to the GameUI abstraction, this class is independent
 * from a specific UI implementation (console, GUI, etc.).
 */
public class ConsoleGame {

    /** Game board model (immutable size during the game) */
    private final Board board;

    /** Main game logic controller */
    private final GameController controller;

    /** User interface abstraction */
    private final GameUI ui;

    /**
     * Creates a new local console game.
     *
     * The board is created using a factory to allow easy switching
     * between different predefined board sizes (e.g. 9x9, 19x19).
     */
    public ConsoleGame(GameUI ui) {
        this.ui = ui;

        // Factory pattern: predefined board configuration (9x9)
        board = BoardFactory.small9();

        // Game controller coordinates rules, turns and validation
        controller = new GameController(
                board,
                new SimpleMoveValidator(),          // Strategy pattern: move validation
                new Player("Black", Stone.Color.BLACK)
        );
    }

    /**
     * Starts the main game loop.
     *
     * Handles:
     * - terminal initialization
     * - user input
     * - game flow (moves, passes, quitting)
     * - board rendering
     */
    public void start() {
        // Enable ANSI escape sequences (required on Windows)
        ConsoleUIFormatter.enableWindowsAnsiSupport();

        // Switch to an alternative screen buffer for cleaner UI
        ConsoleUIFormatter.enterAlternativeScreen();

        try {
            ConsoleUIFormatter.printHeader("GO Game - Local Mode");
            ui.displayBoard(board);

            // Main game loop
            while (true) {
                String input = ui.getMoveInput(
                        "Move (e.g., D4), 'pass', 'quit': "
                );

                // End game on user request
                if (input.equals("quit")) {
                    ConsoleUIFormatter.printInfo("Game ended.");
                    break;
                }

                // Handle pass move
                if (input.equals("pass")) {
                    ConsoleUIFormatter.printMessage("Player passes.");
                    controller.pass();
                    ui.displayBoard(board);
                    continue;
                }

                // Parse move notation (e.g., A3, D10)
                int[] pos = parseMove(input);
                if (pos == null) {
                    ConsoleUIFormatter.printError(
                            "Invalid move format. Use coordinates like D4, A1, etc."
                    );
                    continue;
                }

                // Execute move via controller
                if (!controller.play(pos[0], pos[1])) {
                    ConsoleUIFormatter.printError("Invalid move. Try another position.");
                } else {
                    ConsoleUIFormatter.printSuccess("Move accepted");
                }

                // Refresh board view after each action
                ui.displayBoard(board);
            }
        } finally {
            // Always restore terminal state, even if an error occurs
            ConsoleUIFormatter.exitAlternativeScreen();
            ui.close();
        }
    }

    /**
     * Converts Go-style move notation into board coordinates.
     *
     * Examples:
     *  A1  -> (0, 0)
     *  D4  -> (3, 3)
     *
     * Note:
     *  The letter 'I' is skipped according to Go conventions.
     *
     * @param move user input string
     * @return int array [row, col] or null if input is invalid
     */
    private int[] parseMove(String move) {
        if (move.length() < 2) return null;

        char colChar = Character.toUpperCase(move.charAt(0));

        // Valid columns: A–T without I
        if (colChar < 'A' || colChar > 'T' || colChar == 'I') return null;

        int col = colChar - 'A';

        // Adjust index after skipping 'I'
        if (colChar > 'I') col--;

        try {
            int row = Integer.parseInt(move.substring(1)) - 1;
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
