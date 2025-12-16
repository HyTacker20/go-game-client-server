package com.example.goboard.network;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import com.example.goboard.model.*;
import com.example.goboard.controller.GameController;
import com.example.goboard.network.handler.*;

/**
 * Handles communication with a single client on the server side.
 * Uses Command/Strategy pattern with MessageHandler implementations
 * to reduce complexity and improve maintainability.
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
    
    // Message handlers for different message types
    private final Map<GameMessage.MessageType, MessageHandler> handlers;
    private final MessageHandlerContext handlerContext;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.handlerContext = new MessageHandlerContext(this, server);
        this.handlers = new HashMap<>();
        
        // Register message handlers
        handlers.put(GameMessage.MessageType.JOIN_GAME, new JoinGameHandler());
        handlers.put(GameMessage.MessageType.START_GAME, new StartGameHandler());
        handlers.put(GameMessage.MessageType.MOVE, new MoveHandler());
        handlers.put(GameMessage.MessageType.PASS, new PassHandler());
        handlers.put(GameMessage.MessageType.RESIGN, new ResignHandler());
        
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
            // Normal disconnection
        } catch (ClassNotFoundException | IOException e) {
            // Connection error - will be logged in cleanup if needed
        } finally {
            cleanup();
        }
    }

    private void processMessage(GameMessage message) {
        MessageHandler handler = handlers.get(message.getType());
        if (handler != null) {
            handler.handle(handlerContext, message);
        } else {
            System.out.println("Unknown message type: " + message.getType());
        }
    }

    public int[][] serializeBoard(Board board) {
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
            // Client disconnected or unreachable
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
            GameMessage gameOverMsg = new GameMessage.TextMessage(
                GameMessage.MessageType.GAME_OVER,
                "Opponent disconnected");
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

    public ClientHandler getOpponent() {
        return opponent;
    }
    
    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    public GameController getGameController() {
        return gameController;
    }
    
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
    
    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.playerName = player.getName();
        this.player = player;
    }

    public void initiateGameStart(String opponentName) {
        MessageHandler handler = handlers.get(GameMessage.MessageType.START_GAME);
        GameMessage message = new GameMessage.TextMessage(
            GameMessage.MessageType.START_GAME, opponentName);
        handler.handle(handlerContext, message);
    }
}
