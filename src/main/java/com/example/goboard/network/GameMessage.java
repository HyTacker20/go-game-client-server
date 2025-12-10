package com.example.goboard.network;

import java.io.Serializable;

/**
 * Represents a message sent between client and server.
 * All game-related communication uses this message format.
 */
public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        // Client to Server
        MOVE,           // Player makes a move
        PASS,           // Player passes
        RESIGN,         // Player resigns
        JOIN_GAME,      // Request to join a game
        START_GAME,     // Request to start the game

        // Server to Client
        GAME_STATE,     // Current board state
        MOVE_RESPONSE,  // Response to move attempt
        GAME_OVER,      // Game has ended
        OPPONENT_MOVE,  // Opponent made a move
        OPPONENT_PASS,  // Opponent passed
        ERROR,          // Error message
        WAITING,        // Waiting for opponent
        YOUR_TURN,      // It's your turn
        OPPONENT_TURN   // Opponent's turn
    }

    private MessageType type;
    private int row;
    private int col;
    private String playerName;
    private String message;
    private int[][] boardState;  // Serialized board state
    private String playerColor;  // "BLACK" or "WHITE"
    private boolean success;

    public GameMessage(MessageType type) {
        this.type = type;
    }

    public GameMessage(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public GameMessage(MessageType type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }

    // Getters and Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int[][] getBoardState() {
        return boardState;
    }

    public void setBoardState(int[][] boardState) {
        this.boardState = boardState;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "GameMessage{" +
                "type=" + type +
                ", row=" + row +
                ", col=" + col +
                ", playerName='" + playerName + '\'' +
                ", message='" + message + '\'' +
                ", playerColor='" + playerColor + '\'' +
                ", success=" + success +
                '}';
    }
}
