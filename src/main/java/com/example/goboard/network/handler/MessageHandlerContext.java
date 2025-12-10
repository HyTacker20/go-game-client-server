package com.example.goboard.network.handler;

import com.example.goboard.controller.GameController;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.network.ClientHandler;
import com.example.goboard.network.GameMessage;
import com.example.goboard.network.GameServer;

/**
 * Context object that provides access to client state and utilities
 * for message handlers. This encapsulates the data that handlers need
 * without exposing the entire ClientHandler internals.
 */
public class MessageHandlerContext {
    private final ClientHandler clientHandler;
    private final GameServer server;
    
    public MessageHandlerContext(ClientHandler clientHandler, GameServer server) {
        this.clientHandler = clientHandler;
        this.server = server;
    }
    
    // Getters for handler access
    public String getPlayerName() {
        return clientHandler.getPlayerName();
    }
    
    public Player getPlayer() {
        return clientHandler.getPlayer();
    }
    
    public void setPlayer(Player player) {
        clientHandler.setPlayer(player);
    }
    
    public ClientHandler getOpponent() {
        return clientHandler.getOpponent();
    }
    
    public void setOpponent(ClientHandler opponent) {
        clientHandler.setOpponent(opponent);
    }
    
    public Board getBoard() {
        return clientHandler.getBoard();
    }
    
    public void setBoard(Board board) {
        clientHandler.setBoard(board);
    }
    
    public GameController getGameController() {
        return clientHandler.getGameController();
    }
    
    public void setGameController(GameController controller) {
        clientHandler.setGameController(controller);
    }
    
    public boolean isGameActive() {
        return clientHandler.isGameActive();
    }
    
    public void setGameActive(boolean active) {
        clientHandler.setGameActive(active);
    }
    
    public boolean isAvailable() {
        return clientHandler.isAvailable();
    }
    
    public void setAvailable(boolean available) {
        clientHandler.setAvailable(available);
    }
    
    public void sendMessage(GameMessage message) {
        clientHandler.sendMessage(message);
    }
    
    public int[][] serializeBoard(Board board) {
        return clientHandler.serializeBoard(board);
    }
    
    // Server operations
    public void registerClient(String name, ClientHandler handler) {
        server.registerClient(name, handler);
    }
    
    public ClientHandler findClient(String name) {
        return server.findClient(name);
    }
    
    public GameServer getServer() {
        return server;
    }
    
    public ClientHandler getClientHandler() {
        return clientHandler;
    }
}
