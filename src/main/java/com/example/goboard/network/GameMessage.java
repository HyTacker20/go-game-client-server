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
        SCORE_CONFIRMATION,  // Player confirms or rejects score
        DEAD_STONES,    // Player submits marked dead stones

        // Server to Client
        GAME_STATE,     // Current board state
        MOVE_RESPONSE,  // Response to move attempt
        GAME_OVER,      // Game has ended
        OPPONENT_MOVE,  // Opponent made a move
        OPPONENT_PASS,  // Opponent passed
        SCORING_PHASE,  // Enter scoring phase
        SCORE_RESPONSE, // Score calculation response
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

        public JoinGameMessage(String playerName) {
            super(MessageType.JOIN_GAME);
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        @Override
        public String toString() {
            return "JoinGameMessage{playerName='" + playerName + "'}";
        }
    }

    /**
     * Message containing board state
     */
    public static class BoardStateMessage extends GameMessage {
        private int[][] boardState;
        private String message;
        private int blackCaptured;
        private int whiteCaptured;

        public BoardStateMessage(MessageType type, int[][] boardState) {
            super(type);
            this.boardState = boardState;
            this.blackCaptured = 0;
            this.whiteCaptured = 0;
        }

        public BoardStateMessage(MessageType type, int[][] boardState, String message) {
            super(type);
            this.boardState = boardState;
            this.message = message;
            this.blackCaptured = 0;
            this.whiteCaptured = 0;
        }

        public BoardStateMessage(MessageType type, int[][] boardState, String message, int blackCaptured, int whiteCaptured) {
            super(type);
            this.boardState = boardState;
            this.message = message;
            this.blackCaptured = blackCaptured;
            this.whiteCaptured = whiteCaptured;
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

        public int getBlackCaptured() {
            return blackCaptured;
        }

        public void setBlackCaptured(int blackCaptured) {
            this.blackCaptured = blackCaptured;
        }

        public int getWhiteCaptured() {
            return whiteCaptured;
        }

        public void setWhiteCaptured(int whiteCaptured) {
            this.whiteCaptured = whiteCaptured;
        }

        @Override
        public String toString() {
            return "BoardStateMessage{type=" + getType() + ", message='" + message + "', blackCaptured=" + blackCaptured + ", whiteCaptured=" + whiteCaptured + "}";
        }
    }

    /**
     * Message for move response with success status
     */
    public static class MoveResponseMessage extends GameMessage {
        private boolean success;
        private String message;
        private int[][] boardState;
        private int blackCaptured;
        private int whiteCaptured;

        public MoveResponseMessage(boolean success, String message, int[][] boardState) {
            super(MessageType.MOVE_RESPONSE);
            this.success = success;
            this.message = message;
            this.boardState = boardState;
            this.blackCaptured = 0;
            this.whiteCaptured = 0;
        }

        public MoveResponseMessage(boolean success, String message, int[][] boardState, int blackCaptured, int whiteCaptured) {
            super(MessageType.MOVE_RESPONSE);
            this.success = success;
            this.message = message;
            this.boardState = boardState;
            this.blackCaptured = blackCaptured;
            this.whiteCaptured = whiteCaptured;
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

        public int getBlackCaptured() {
            return blackCaptured;
        }

        public void setBlackCaptured(int blackCaptured) {
            this.blackCaptured = blackCaptured;
        }

        public int getWhiteCaptured() {
            return whiteCaptured;
        }

        public void setWhiteCaptured(int whiteCaptured) {
            this.whiteCaptured = whiteCaptured;
        }

        @Override
        public String toString() {
            return "MoveResponseMessage{success=" + success + ", message='" + message + "', blackCaptured=" + blackCaptured + ", whiteCaptured=" + whiteCaptured + "}";
        }
    }

    /**
     * Message carrying dead-stone coordinates from client to server.
     */
    public static class DeadStonesMessage extends GameMessage {
        private int[][] positions; // [ [row,col], ... ]

        public DeadStonesMessage(int[][] positions) {
            super(MessageType.DEAD_STONES);
            this.positions = positions;
        }

        public int[][] getPositions() {
            return positions;
        }

        public void setPositions(int[][] positions) {
            this.positions = positions;
        }

        @Override
        public String toString() {
            return "DeadStonesMessage{count=" + (positions == null ? 0 : positions.length) + "}";
        }
    }

    /**
     * Message for score calculation with final scores
     */
    public static class ScoreMessage extends GameMessage {
        private double blackScore;
        private double whiteScore;
        private int[][] boardState;
        private String blackPlayerName;
        private String whitePlayerName;

        public ScoreMessage(MessageType type, double blackScore, double whiteScore, 
                           int[][] boardState, String blackPlayerName, String whitePlayerName) {
            super(type);
            this.blackScore = blackScore;
            this.whiteScore = whiteScore;
            this.boardState = boardState;
            this.blackPlayerName = blackPlayerName;
            this.whitePlayerName = whitePlayerName;
        }

        public double getBlackScore() {
            return blackScore;
        }

        public double getWhiteScore() {
            return whiteScore;
        }

        public int[][] getBoardState() {
            return boardState;
        }

        public String getBlackPlayerName() {
            return blackPlayerName;
        }

        public String getWhitePlayerName() {
            return whitePlayerName;
        }

        @Override
        public String toString() {
            return "ScoreMessage{type=" + getType() + ", black=" + blackScore + 
                   ", white=" + whiteScore + "}";
        }
    }

    /**
     * Message for score confirmation/rejection
     */
    public static class ScoreConfirmationMessage extends GameMessage {
        private boolean accepted;

        public ScoreConfirmationMessage(boolean accepted) {
            super(MessageType.SCORE_CONFIRMATION);
            this.accepted = accepted;
        }

        public boolean isAccepted() {
            return accepted;
        }

        @Override
        public String toString() {
            return "ScoreConfirmationMessage{accepted=" + accepted + "}";
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
        private int blackCaptured;
        private int whiteCaptured;

        public OpponentMoveMessage(int row, int col, String message, int[][] boardState) {
            super(MessageType.OPPONENT_MOVE);
            this.row = row;
            this.col = col;
            this.message = message;
            this.boardState = boardState;
            this.blackCaptured = 0;
            this.whiteCaptured = 0;
        }

        public OpponentMoveMessage(int row, int col, String message, int[][] boardState, int blackCaptured, int whiteCaptured) {
            super(MessageType.OPPONENT_MOVE);
            this.row = row;
            this.col = col;
            this.message = message;
            this.boardState = boardState;
            this.blackCaptured = blackCaptured;
            this.whiteCaptured = whiteCaptured;
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

        public int getBlackCaptured() {
            return blackCaptured;
        }

        public void setBlackCaptured(int blackCaptured) {
            this.blackCaptured = blackCaptured;
        }

        public int getWhiteCaptured() {
            return whiteCaptured;
        }

        public void setWhiteCaptured(int whiteCaptured) {
            this.whiteCaptured = whiteCaptured;
        }

        @Override
        public String toString() {
            return "OpponentMoveMessage{row=" + row + ", col=" + col + ", message='" + message + "', blackCaptured=" + blackCaptured + ", whiteCaptured=" + whiteCaptured + "}";
        }
    }
}
