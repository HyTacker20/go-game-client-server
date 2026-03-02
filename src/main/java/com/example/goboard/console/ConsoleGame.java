package com.example.goboard.console;

import com.example.goboard.controller.GameController;
import com.example.goboard.factory.BoardFactory;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.SimpleMoveValidator;
import com.example.goboard.view.GameUI;
import com.example.goboard.view.ConsoleUIFormatter;

public class ConsoleGame {

    private final Board board;
    private final GameController controller;
    private final GameUI ui;

    public ConsoleGame(GameUI ui) {
        this.ui = ui;

        this.board = BoardFactory.small9();

        Player black = Player.defaultBlack();
        Player white = Player.defaultWhite();

        this.controller = new GameController(
                board,
                new SimpleMoveValidator(),
                black,
                white
        );
    }

    public void start() {
        ConsoleUIFormatter.enableWindowsAnsiSupport();
        ConsoleUIFormatter.enterAlternativeScreen();

        try {
            ConsoleUIFormatter.printHeader("GO Game - Local Mode");
            ui.displayBoard(board);

            gameLoop();

            ConsoleUIFormatter.printInfo("Game over.");

        } finally {
            ConsoleUIFormatter.exitAlternativeScreen();
            ui.close();
        }
    }

    private void gameLoop() {

        while (!controller.isGameOver()) {

            // === SCORING PHASE ===
            if (controller.isScoringPhase()) {
                controller.handleScoring(ui);
                ui.displayBoard(board);
                continue;
            }

            // === NORMAL PLAY ===
            String prompt =
                    controller.getCurrentPlayer().getName()
                            + " move (e.g. D4), 'pass', 'resign', 'quit': ";

            String input = ui.getMoveInput(prompt).trim();

            // === PLAYER RESIGNS ===
            // A player may resign at any moment of the game.
            // The opponent immediately wins.
            if (input.equalsIgnoreCase("resign")) {
                ConsoleUIFormatter.printMessage(
                        controller.getCurrentPlayer().getName()
                                + " resigns."
                );

                Player winner =
                        controller.getCurrentPlayer().getColor() == Stone.Color.BLACK
                                ? controller.getWhitePlayer()
                                : controller.getBlackPlayer();

                ConsoleUIFormatter.printSuccess(
                        winner.getName() + " wins by resignation."
                );
                return;
            }

            if (input.equalsIgnoreCase("quit")) {
                return;
            }

            if (input.equalsIgnoreCase("pass")) {
                ConsoleUIFormatter.printMessage(
                        controller.getCurrentPlayer().getName() + " passes."
                );
                controller.pass();
                ui.displayBoard(board);
                continue;
            }

            int[] pos = parseMove(input);
            if (pos == null) {
                ConsoleUIFormatter.printError(
                        "Invalid move format. Use A1–T19 (I skipped)."
                );
                continue;
            }

            boolean ok = controller.play(pos[0], pos[1]);
            if (!ok) {
                ConsoleUIFormatter.printError("Illegal move.");
            } else {
                ConsoleUIFormatter.printSuccess("Move accepted.");
            }

            ui.displayBoard(board);
        }
    }

    /**
     * Parses moves like "D4" → [row, col]
     */
    private int[] parseMove(String move) {
        if (move.length() < 2) return null;

        char colChar = Character.toUpperCase(move.charAt(0));
        if (colChar < 'A' || colChar > 'T' || colChar == 'I') return null;

        int col = colChar - 'A';
        if (colChar > 'I') col--; // skip I

        try {
            int row = Integer.parseInt(move.substring(1)) - 1;
            if (row < 0 || row >= board.getSize()) return null;
            if (col < 0 || col >= board.getSize()) return null;
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
