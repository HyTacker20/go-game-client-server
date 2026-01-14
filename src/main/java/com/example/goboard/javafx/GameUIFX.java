package com.example.goboard.javafx;

import com.example.goboard.model.Board;
import com.example.goboard.view.GameUI;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JavaFX implementation of the GameUI interface.
 * 
 * This adapter allows the existing game controller and network code
 * to work with JavaFX UI components. It bridges synchronous GameUI
 * methods with asynchronous JavaFX event handling.
 * 
 * Note: This implementation is primarily for future integration with
 * GameController. The current JavaFXGameClient handles UI directly
 * for better responsiveness in networked gameplay.
 */
public class GameUIFX implements GameUI {
    
    private BoardView boardView;
    private BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<int[]> selectionQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Boolean> confirmationQueue = new LinkedBlockingQueue<>();
    
    public GameUIFX(BoardView boardView) {
        this.boardView = boardView;
    }
    
    @Override
    public void displayBoard(Board board) {
        Platform.runLater(() -> {
            boardView.setBoard(board);
            boardView.setScoringMode(false);
            boardView.render();
        });
    }
    
    @Override
    public void displayScoringBoard(Board board) {
        Platform.runLater(() -> {
            boardView.setBoard(board);
            boardView.setScoringMode(true);
            boardView.render();
        });
    }
    
    @Override
    public String getMoveInput(String prompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Move Input");
            dialog.setHeaderText(prompt);
            dialog.setContentText("Enter move (e.g., 'D4', 'pass', 'quit'):");
            
            Optional<String> result = dialog.showAndWait();
            future.complete(result.orElse("quit"));
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return "quit";
        }
    }
    
    @Override
    public void displayMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
    
    @Override
    public String getStringInput(String prompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input Required");
            dialog.setHeaderText(prompt);
            
            Optional<String> result = dialog.showAndWait();
            future.complete(result.orElse(""));
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return "";
        }
    }
    
    @Override
    public int getIntegerInput(String prompt, int min, int max) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            while (true) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Integer Input");
                dialog.setHeaderText(prompt);
                dialog.setContentText(String.format("Enter a number between %d and %d:", min, max));
                
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        int value = Integer.parseInt(result.get().trim());
                        if (value >= min && value <= max) {
                            future.complete(value);
                            return;
                        } else {
                            showError("Invalid Input", 
                                String.format("Please enter a number between %d and %d", min, max));
                        }
                    } catch (NumberFormatException e) {
                        showError("Invalid Input", "Please enter a valid number");
                    }
                } else {
                    future.complete(min); // Default to min if cancelled
                    return;
                }
            }
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return min;
        }
    }
    
    @Override
    public int[] getDeadGroupSelection(String prompt) {
        CompletableFuture<int[]> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mark Dead Stones");
            alert.setHeaderText(prompt);
            alert.setContentText("Click on stones to mark/unmark them as dead.\n" +
                               "Close this dialog when finished.");
            
            boardView.setScoringMode(true);
            
            // Set up click handler for dead stone marking
            boardView.setOnIntersectionClick((row, col) -> {
                boardView.toggleDeadStone(row, col);
            });
            
            alert.showAndWait();
            
            // User finished marking - return null to indicate completion
            future.complete(null);
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public void displayScore(int blackScore, int whiteScore) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Score");
            alert.setHeaderText("Current Score");
            alert.setContentText(String.format(
                "Black: %d\nWhite: %d\n\nDifference: %d", 
                blackScore, whiteScore, Math.abs(blackScore - whiteScore)
            ));
            alert.showAndWait();
        });
    }
    
    @Override
    public boolean confirmScore(String playerName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Score");
            alert.setHeaderText(playerName + ", do you accept the score?");
            alert.setContentText("Select OK to accept, or Cancel to resume the game.");
            
            Optional<ButtonType> result = alert.showAndWait();
            future.complete(result.isPresent() && result.get() == ButtonType.OK);
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return true; // Default to accepting
        }
    }
    
    @Override
    public void close() {
        // JavaFX resources are managed by the Application lifecycle
        // No explicit cleanup needed here
    }
    
    /**
     * Show an error dialog.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Enable click-to-move mode for normal gameplay.
     * This sets up the board to accept move inputs via mouse clicks.
     */
    public void enableMoveInput(java.util.function.BiConsumer<Integer, Integer> moveHandler) {
        Platform.runLater(() -> {
            boardView.setScoringMode(false);
            boardView.setOnIntersectionClick(moveHandler);
        });
    }
    
    /**
     * Disable all board interactions.
     */
    public void disableInput() {
        Platform.runLater(() -> {
            boardView.setOnIntersectionClick(null);
        });
    }
}
