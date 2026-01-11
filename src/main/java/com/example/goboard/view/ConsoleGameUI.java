package com.example.goboard.view;

import java.util.Scanner;
import com.example.goboard.model.Board;

/**
 * Console implementation of GameUI.
 * Handles all console-based UI operations for the GO game.
 */
public class ConsoleGameUI implements GameUI {

    private final Scanner scanner;
    private final BoardRenderer renderer;

    public ConsoleGameUI() {
        this.scanner = new Scanner(System.in);
        this.renderer = new AsciiBoardRenderer();
    }

    @Override
    public void displayBoard(Board board) {
        ConsoleUIFormatter.clearScreen();
        String boardString = renderer.render(board);
        ConsoleUIFormatter.printBoardWithFrame(boardString);
    }

    /**
     * Display board during scoring phase.
     * Currently identical to normal board display.
     */
    @Override
    public void displayScoringBoard(Board board) {
        displayBoard(board);
    }

    @Override
    public String getMoveInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim().toLowerCase();
    }

    @Override
    public void displayMessage(String message) {
        ConsoleUIFormatter.printMessage(message);
    }

    @Override
    public String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    @Override
    public int getIntegerInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                ConsoleUIFormatter.printWarning(
                        "Please enter a value between " + min + " and " + max
                );
            } catch (NumberFormatException e) {
                ConsoleUIFormatter.printError("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Ask user to select a group to mark as dead.
     * User can press ENTER to finish marking.
     */
    @Override
    public int[] getDeadGroupSelection(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return null; // finished marking
        }

        try {
            char colChar = Character.toUpperCase(input.charAt(0));
            int col = colChar - 'A';
            int row = Integer.parseInt(input.substring(1)) - 1;
            return new int[]{row, col};
        } catch (Exception e) {
            ConsoleUIFormatter.printError("Invalid position format.");
            return null;
        }
    }

    /**
     * Display final score.
     */
    @Override
    public void displayScore(int blackScore, int whiteScore) {
        ConsoleUIFormatter.printHeader("Final Score");
        System.out.println("Black: " + blackScore);
        System.out.println("White: " + whiteScore);
    }

    /**
     * Ask player to confirm the calculated score.
     * Returns true if player accepts, false if they request game resumption.
     */
    @Override
    public boolean confirmScore(String playerName) {
        while (true) {
            System.out.print(playerName + ", do you accept the final score? (Y/N): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y")) return true;
            if (input.equals("N")) return false;
            ConsoleUIFormatter.printError("Please enter Y (yes) or N (no).");
        }
    }

    @Override
    public void close() {
        scanner.close();
    }
}
