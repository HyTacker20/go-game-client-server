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
        String boardString = renderer.render(board);
        ConsoleUIFormatter.printBoardWithFrame(boardString);
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
                ConsoleUIFormatter.printWarning("Please enter a value between " + min + " and " + max);
            } catch (NumberFormatException e) {
                ConsoleUIFormatter.printError("Invalid input. Please enter a number.");
            }
        }
    }
    
    @Override
    public void close() {
        scanner.close();
    }
}
