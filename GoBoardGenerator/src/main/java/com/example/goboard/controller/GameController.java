package com.example.goboard.controller;

import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.model.Intersection;
import com.example.goboard.strategy.MoveValidator;

/**
 * Kontroler gry: obsługuje wykonywanie ruchów oraz pasów.
 * Zakończenie gry następuje po dwóch kolejnych pasach (proste zachowanie konsolowe).
 */
public class GameController {
    private final Board board;
    private final MoveValidator validator;

    private final Player blackPlayer;
    private final Player whitePlayer;
    private Player currentPlayer;

    private int consecutivePasses = 0;
    private boolean gameOver = false;

    public GameController(Board board, MoveValidator validator, Player black, Player white, Player starting) {
        this.board = board;
        this.validator = validator;
        this.blackPlayer = black;
        this.whitePlayer = white;
        this.currentPlayer = starting != null ? starting : black;
    }

    /**
     * Wariant konstrukcyjny dla prostszych wywołań: podajemy tylko jednego gracza startowego.
     * Drugi gracz utworzony jest automatycznie z odwrotnym kolorem i nazwą \"White\" (jeśli potrzeba, zmień).
     */
    public GameController(Board board, MoveValidator validator, Player starting) {
        this.board = board;
        this.validator = validator;
        this.blackPlayer = starting.getColor() == Stone.Color.BLACK ? starting : new Player("Black", Stone.Color.BLACK);
        this.whitePlayer = starting.getColor() == Stone.Color.WHITE ? starting : new Player("White", Stone.Color.WHITE);
        this.currentPlayer = starting;
    }

    /**
     * Wykonaj ruch (postaw kamień). Zwraca true gdy ruch poprawny i wykonany.
     */
    public boolean play(int row, int col) {
        if (gameOver) return false;
        Stone stone = new Stone(currentPlayer.getColor());
        if (!validator.isValid(board, row, col, stone)) return false;

        boolean ok = board.placeStone(row, col, stone);
        if (ok) {
            // po poprawnym ruchu resetujemy liczbę pasów
            consecutivePasses = 0;
            swapPlayer();
        }
        return ok;
    }

    /**
     * Gracz pasuje. Zwraca true jeśli gra się zakończyła (np. po dwóch pasach).
     */
    public boolean pass() {
        if (gameOver) return true;
        consecutivePasses++;
        if (consecutivePasses >= 2) {
            // proste zakończenie gry po dwóch pasach
            gameOver = true;
            return true;
        } else {
            swapPlayer();
            return false;
        }
    }

    private void swapPlayer() {
        if (currentPlayer == blackPlayer) currentPlayer = whitePlayer;
        else currentPlayer = blackPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getConsecutivePasses() {
        return consecutivePasses;
    }
}
