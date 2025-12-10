package com.example.goboard.network;

import java.io.*;
import java.net.Socket;
import com.example.goboard.model.*;
import com.example.goboard.factory.BoardFactory;
import com.example.goboard.strategy.SimpleMoveValidator;
import com.example.goboard.controller.GameController;

/**
 * Handles communication with a single client on the server side.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private GameServer server;
    private String playerName;
    private ClientHandler opponent;
    private GameController gameController;
    private Board board;
    private Player player;
    private boolean available = false;
    private boolean gameActive = false;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error initializing streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                GameMessage message = (GameMessage) in.readObject();
                processMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("Client " + playerName + " disconnected");
        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Error processing message from " + playerName + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void processMessage(GameMessage message) {
        switch (message.getType()) {
            case JOIN_GAME:
                handleJoinGame(message);
                break;
            case START_GAME:
                handleStartGame(message);
                break;
            case MOVE:
                handleMove(message);
                break;
            case PASS:
                handlePass(message);
                break;
            case RESIGN:
                handleResign(message);
                break;
            default:
                System.out.println("Unknown message type: " + message.getType());
        }
    }

    private void handleJoinGame(GameMessage message) {
        this.playerName = message.getPlayerName();
        String color = message.getPlayerColor();
        
        server.registerClient(playerName, this);
        
        // Create player with specified color
        Stone.Color stoneColor = "WHITE".equalsIgnoreCase(color) ? 
            Stone.Color.WHITE : Stone.Color.BLACK;
        this.player = new Player(playerName, stoneColor);
        
        GameMessage response = new GameMessage(GameMessage.MessageType.WAITING);
        response.setMessage("Waiting for opponent...");
        sendMessage(response);
        
        System.out.println("Player " + playerName + " (" + stoneColor + ") joined the game");
        
        // Mark as available for matching
        this.available = true;
    }

    private void handleStartGame(GameMessage message) {
        String opponentName = message.getMessage();
        opponent = server.findClient(opponentName);
        
        if (opponent == null) {
            GameMessage response = new GameMessage(GameMessage.MessageType.ERROR);
            response.setMessage("Opponent not found: " + opponentName);
            sendMessage(response);
            return;
        }
        
        if (opponent.isGameActive()) {
            GameMessage response = new GameMessage(GameMessage.MessageType.ERROR);
            response.setMessage("Opponent is already in a game");
            sendMessage(response);
            return;
        }
        
        // Initialize game
        board = BoardFactory.standard19();
        
        // Determine colors based on player setup
        Player blackPlayer = this.player.getColor() == Stone.Color.BLACK ? 
            this.player : opponent.getPlayer();
        Player whitePlayer = this.player.getColor() == Stone.Color.WHITE ? 
            this.player : opponent.getPlayer();
        
        gameController = new GameController(board, new SimpleMoveValidator(), 
            blackPlayer, whitePlayer, blackPlayer);
        
        this.gameActive = true;
        opponent.setGameActive(true);
        opponent.setOpponent(this);
        opponent.setGameController(gameController);
        opponent.setBoard(board);
        
        available = false;
        opponent.setAvailable(false);
        
        // Notify both players
        GameMessage startMsg = new GameMessage(GameMessage.MessageType.YOUR_TURN);
        startMsg.setMessage("Game started! Black plays first");
        startMsg.setBoardState(serializeBoard(board));
        
        if (this.player.getColor() == Stone.Color.BLACK) {
            sendMessage(startMsg);
            
            GameMessage opponentMsg = new GameMessage(GameMessage.MessageType.OPPONENT_TURN);
            opponentMsg.setMessage("Game started! Opponent (Black) plays first");
            opponentMsg.setBoardState(serializeBoard(board));
            opponent.sendMessage(opponentMsg);
        } else {
            sendMessage(new GameMessage(GameMessage.MessageType.OPPONENT_TURN, "Game started! Opponent (Black) plays first"));
            opponent.sendMessage(startMsg);
        }
        
        System.out.println("Game started: " + this.playerName + " vs " + opponent.getPlayerName());
    }

    private void handleMove(GameMessage message) {
        if (!gameActive || opponent == null) {
            GameMessage response = new GameMessage(GameMessage.MessageType.ERROR);
            response.setMessage("Game not active");
            sendMessage(response);
            return;
        }
        
        int row = message.getRow();
        int col = message.getCol();
        
        boolean success = gameController.play(row, col);
        
        GameMessage response = new GameMessage(GameMessage.MessageType.MOVE_RESPONSE);
        response.setSuccess(success);
        response.setBoardState(serializeBoard(board));
        
        if (success) {
            response.setMessage("Move accepted at (" + row + ", " + col + ")");
            sendMessage(response);
            
            // Notify opponent
            GameMessage opponentMsg = new GameMessage(GameMessage.MessageType.OPPONENT_MOVE);
            opponentMsg.setRow(row);
            opponentMsg.setCol(col);
            opponentMsg.setBoardState(serializeBoard(board));
            opponentMsg.setMessage("Opponent played at (" + row + ", " + col + ")");
            opponent.sendMessage(opponentMsg);
        } else {
            response.setMessage("Invalid move at (" + row + ", " + col + ")");
            sendMessage(response);
        }
    }

    private void handlePass(GameMessage message) {
        if (!gameActive || opponent == null) {
            GameMessage response = new GameMessage(GameMessage.MessageType.ERROR);
            response.setMessage("Game not active");
            sendMessage(response);
            return;
        }
        
        gameController.pass();
        
        GameMessage response = new GameMessage(GameMessage.MessageType.MOVE_RESPONSE);
        response.setSuccess(true);
        response.setMessage("You passed");
        response.setBoardState(serializeBoard(board));
        sendMessage(response);
        
        // Notify opponent
        GameMessage opponentMsg = new GameMessage(GameMessage.MessageType.OPPONENT_PASS);
        opponentMsg.setMessage("Opponent passed");
        opponentMsg.setBoardState(serializeBoard(board));
        opponent.sendMessage(opponentMsg);
    }

    private void handleResign(GameMessage message) {
        if (gameActive && opponent != null) {
            GameMessage gameOverMsg = new GameMessage(GameMessage.MessageType.GAME_OVER);
            gameOverMsg.setMessage("Opponent resigned. You win!");
            opponent.sendMessage(gameOverMsg);
            
            GameMessage selfMsg = new GameMessage(GameMessage.MessageType.GAME_OVER);
            selfMsg.setMessage("You resigned. You lost.");
            sendMessage(selfMsg);
            
            endGame();
        }
    }

    private void endGame() {
        gameActive = false;
        if (opponent != null) {
            opponent.setGameActive(false);
        }
        available = true;
        if (opponent != null) {
            opponent.setAvailable(true);
        }
    }

    private int[][] serializeBoard(Board board) {
        int size = board.getSize();
        int[][] state = new int[size][size];
        
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection inter = board.getIntersection(r, c);
                if (inter.isEmpty()) {
                    state[r][c] = 0;  // Empty
                } else {
                    Stone stone = inter.getStone();
                    state[r][c] = stone.getColor() == Stone.Color.BLACK ? 1 : 2;
                }
            }
        }
        return state;
    }

    public void sendMessage(GameMessage message) {
        try {
            synchronized (out) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to " + playerName + ": " + e.getMessage());
        }
    }

    private void cleanup() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        
        if (playerName != null) {
            server.unregisterClient(playerName);
            server.removeHandler(this);
        }
        
        if (opponent != null && gameActive) {
            GameMessage gameOverMsg = new GameMessage(GameMessage.MessageType.GAME_OVER);
            gameOverMsg.setMessage("Opponent disconnected");
            opponent.sendMessage(gameOverMsg);
        }
    }

    // Getters and Setters
    public String getPlayerName() {
        return playerName;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void setGameActive(boolean gameActive) {
        this.gameActive = gameActive;
    }

    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Player getPlayer() {
        return player;
    }

    public void initiateGameStart(GameMessage message) {
        handleStartGame(message);
    }
}
