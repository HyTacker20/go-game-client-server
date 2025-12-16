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
        // Enable ANSI support and enter alternative screen buffer
        ConsoleUIFormatter.enableWindowsAnsiSupport();
        ConsoleUIFormatter.enterAlternativeScreen();
        
        try {
            ConsoleUIFormatter.printHeader("GO Game - Local Mode");
            ui.displayBoard(board);

            while (true) {
            String input = ui.getMoveInput("Move (e.g., D4), 'pass', 'quit': ");

                if (input.equals("quit")) {
                    ConsoleUIFormatter.printInfo("Game ended.");
                    break;
                }

            if (input.equals("pass")) {
                ConsoleUIFormatter.printMessage("Player passes.");
                controller.pass();
                ui.displayBoard(board);
                continue;
            }

            int[] pos = parseMove(input);
            if (pos == null) {
                ConsoleUIFormatter.printError("Invalid move format. Use coordinates like D4, A1, etc.");
                continue;
            }

            if (!controller.play(pos[0], pos[1])) {
                ConsoleUIFormatter.printError("Invalid move. Try another position.");
            } else {
                ConsoleUIFormatter.printSuccess("Move accepted");
            }

                ui.displayBoard(board);
            }
        } finally {
            // Always restore the original terminal screen on exit
            ConsoleUIFormatter.exitAlternativeScreen();
            ui.close();
        }
    }

    // Parse move notation (e.g., A3, D10). Note: skips I column in Go
    private int[] parseMove(String move) {
        if (move.length() < 2) return null;

        char colChar = Character.toUpperCase(move.charAt(0));
        if (colChar < 'A' || colChar > 'T' || colChar == 'I') return null;

        int col = colChar - 'A';
        if (colChar > 'I') col--;

        try {
            int row = Integer.parseInt(move.substring(1)) - 1;
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
