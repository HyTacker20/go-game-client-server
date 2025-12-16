package com.example.goboard.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import com.example.goboard.model.*;
import com.example.goboard.factory.BoardFactory;
import com.example.goboard.strategy.SimpleMoveValidator;
import com.example.goboard.controller.GameController;

/**
 * Game server that manages client connections and game logic.
 * Handles multiple games and player connections.
 */
public class GameServer {
    private static final int PORT = 5555;
    private final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private boolean running = false;
    private final Map<String, ClientHandler> clientByName = Collections.synchronizedMap(new HashMap<>());
    private final Object matchLock = new Object();

    public GameServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Game Server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    public void start() {
        running = true;
        Thread acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] ✓ New client connected from " + clientSocket.getInetAddress());
                    
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    connectedClients.add(handler);
                    
                    Thread clientThread = new Thread(handler);
                    clientThread.start();
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Socket error: " + e.getMessage());
                    }
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        });
        
        acceptThread.setName("ServerAcceptThread");
        acceptThread.start();
        
        // Start game matching thread
        Thread matchingThread = new Thread(this::matchPlayers);
        matchingThread.setName("GameMatchingThread");
        matchingThread.setDaemon(true);
        matchingThread.start();
        
        // Keep server running
        try {
            acceptThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void matchPlayers() {
        while (running) {
            try {
                Thread.sleep(1000); // Check every second
                
                synchronized (matchLock) {
                    List<ClientHandler> available = new ArrayList<>();
                    for (ClientHandler handler : connectedClients) {
                        if (handler.isAvailable() && !handler.isGameActive()) {
                            available.add(handler);
                        }
                    }
                    
                    // Match players in pairs
                    while (available.size() >= 2) {
                        ClientHandler player1 = available.remove(0);
                        ClientHandler player2 = available.remove(0);
                        
                        startGame(player1, player2);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void startGame(ClientHandler player1, ClientHandler player2) {
        System.out.println("\n[SERVER] ═══════════════════════════════════════");
        System.out.println("[SERVER] ⚔  Starting new game");
        System.out.println("[SERVER]    Player 1: " + player1.getPlayerName());
        System.out.println("[SERVER]    Player 2: " + player2.getPlayerName());
        System.out.println("[SERVER] ═══════════════════════════════════════\n");
        
        // Set both players as unavailable
        player1.setAvailable(false);
        player2.setAvailable(false);
        
        // Initialize game through player1
        player1.initiateGameStart(player2.getPlayerName());
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    public void registerClient(String name, ClientHandler handler) {
        clientByName.put(name, handler);
        System.out.println("[SERVER] ► " + name + " registered and ready");
    }

    public void unregisterClient(String name) {
        clientByName.remove(name);
        if (name != null) {
            System.out.println("[SERVER] ◄ " + name + " disconnected");
        }
    }

    public ClientHandler findClient(String name) {
        return clientByName.get(name);
    }

    public void removeHandler(ClientHandler handler) {
        connectedClients.remove(handler);
    }

    public List<String> getAvailablePlayers() {
        List<String> available = new ArrayList<>();
        for (ClientHandler handler : connectedClients) {
            if (handler.isAvailable()) {
                available.add(handler.getPlayerName());
            }
        }
        System.out.println("[GameServer] Available players: " + available);
        return available;
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
        
        // Shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));
    }
}
