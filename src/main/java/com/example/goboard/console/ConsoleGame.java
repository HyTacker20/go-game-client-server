package com.example.goboard.console;

import com.example.goboard.controller.GameController;
import com.example.goboard.factory.BoardFactory;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.SimpleMoveValidator;
import com.example.goboard.view.GameUI;

/**
 * Local game controller that uses a GameUI abstraction.
 * This allows the game to work with any UI implementation (Console, GUI, etc.)
 */
public class ConsoleGame {

    private final Board board;
    private final GameController controller;
    private final GameUI ui;

    public ConsoleGame(GameUI ui) {
        this.ui = ui;
        board = BoardFactory.small9();
        controller = new GameController(
                board,
                new SimpleMoveValidator(),
                new Player("Black", Stone.Color.BLACK)
        );
    }

    public void start() {
        ui.displayMessage("=== GO Game ===");
        ui.displayBoard(board);

        while (true) {
            String input = ui.getMoveInput("Move (e.g., D4), 'pass', 'quit': ");

            if (input.equals("quit")) {
                ui.displayMessage("Game ended.");
                break;
            }

            if (input.equals("pass")) {
                ui.displayMessage("Player passes.");
                controller.pass();
                continue;
            }

            int[] pos = parseMove(input);
            if (pos == null) {
                ui.displayMessage("Invalid move format.");
                continue;
            }

            if (!controller.play(pos[0], pos[1])) {
                ui.displayMessage("Invalid move.");
            }

            ui.displayBoard(board);
        }
        
        ui.close();
    }

    /**
     * Parsuje notację typu A3, D10 itp.
     * W GO pomija się literę I.
     */
    private int[] parseMove(String move) {
        if (move.length() < 2) return null;

        char colChar = Character.toUpperCase(move.charAt(0));
        if (colChar < 'A' || colChar > 'T' || colChar == 'I') return null;

        int col = colChar - 'A';
        if (colChar > 'I') col--; // pominięcie I

        try {
            int row = Integer.parseInt(move.substring(1)) - 1; // w kodzie start od 0
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
