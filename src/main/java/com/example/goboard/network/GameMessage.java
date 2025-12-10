package com.example.goboard.network;

import java.io.Serializable;

/**
 * Base class for all game messages sent between client and server.
 * Uses inheritance hierarchy to avoid god object anti-pattern.
 */
public abstract class GameMessage implements Serializable {
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

    private final MessageType type;

    protected GameMessage(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "GameMessage{type=" + type + "}";
    }

    // Factory method for backward compatibility during migration
    public static GameMessage create(MessageType type) {
        return new SimpleMessage(type);
    }

    public static GameMessage create(MessageType type, String message) {
        return new TextMessage(type, message);
    }

    // Specific message type classes

    /**
     * Simple message with no additional data
     */
    public static class SimpleMessage extends GameMessage {
        public SimpleMessage(MessageType type) {
            super(type);
        }
    }

    /**
     * Message with text content
     */
    public static class TextMessage extends GameMessage {
        private String message;

        public TextMessage(MessageType type, String message) {
            super(type);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "TextMessage{type=" + getType() + ", message='" + message + "'}";
        }
    }

    /**
     * Message for move actions with row and column
     */
    public static class MoveMessage extends GameMessage {
        private int row;
        private int col;

        public MoveMessage(MessageType type, int row, int col) {
            super(type);
            this.row = row;
            this.col = col;
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

        @Override
        public String toString() {
            return "MoveMessage{type=" + getType() + ", row=" + row + ", col=" + col + "}";
        }
    }

    /**
     * Message for joining game with player info
     */
    public static class JoinGameMessage extends GameMessage {
        private String playerName;
        private String playerColor;

        public JoinGameMessage(String playerName, String playerColor) {
            super(MessageType.JOIN_GAME);
            this.playerName = playerName;
            this.playerColor = playerColor;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerColor() {
            return playerColor;
        }

        public void setPlayerColor(String playerColor) {
            this.playerColor = playerColor;
        }

        @Override
        public String toString() {
            return "JoinGameMessage{playerName='" + playerName + "', playerColor='" + playerColor + "'}";
        }
    }

    /**
     * Message containing board state
     */
    public static class BoardStateMessage extends GameMessage {
        private int[][] boardState;
        private String message;

        public BoardStateMessage(MessageType type, int[][] boardState) {
            super(type);
            this.boardState = boardState;
        }

        public BoardStateMessage(MessageType type, int[][] boardState, String message) {
            super(type);
            this.boardState = boardState;
            this.message = message;
        }

        public int[][] getBoardState() {
            return boardState;
        }

        public void setBoardState(int[][] boardState) {
            this.boardState = boardState;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "BoardStateMessage{type=" + getType() + ", message='" + message + "'}";
        }
    }

    /**
     * Message for move response with success status
     */
    public static class MoveResponseMessage extends GameMessage {
        private boolean success;
        private String message;
        private int[][] boardState;

        public MoveResponseMessage(boolean success, String message, int[][] boardState) {
            super(MessageType.MOVE_RESPONSE);
            this.success = success;
            this.message = message;
            this.boardState = boardState;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
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

        @Override
        public String toString() {
            return "MoveResponseMessage{success=" + success + ", message='" + message + "'}";
        }
    }

    /**
     * Message for opponent move with coordinates and board state
     */
    public static class OpponentMoveMessage extends GameMessage {
        private int row;
        private int col;
        private String message;
        private int[][] boardState;

        public OpponentMoveMessage(int row, int col, String message, int[][] boardState) {
            super(MessageType.OPPONENT_MOVE);
            this.row = row;
            this.col = col;
            this.message = message;
            this.boardState = boardState;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public String getMessage() {
            return message;
        }

        public int[][] getBoardState() {
            return boardState;
        }

        @Override
        public String toString() {
            return "OpponentMoveMessage{row=" + row + ", col=" + col + ", message='" + message + "'}";
        }
    }
}
