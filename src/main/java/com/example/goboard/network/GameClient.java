package com.example.goboard.network;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;
import com.example.goboard.view.AsciiBoardRenderer;

/**
 * Game client that connects to the server and handles local player input.
 */
public class GameClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;
    private String playerColor;
    private Board board;
    private boolean connected = false;
    private boolean gameActive = false;
    private boolean myTurn = false;
    private final AsciiBoardRenderer renderer = new AsciiBoardRenderer();
    private final Scanner scanner = new Scanner(System.in);

    public GameClient(String name, String color) {
        this.playerName = name;
        this.playerColor = color;
        this.board = new Board(9);
    }

    public GameClient(String name) {
        this.playerName = name;
        this.playerColor = "RANDOM";
        this.board = new Board(9);
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            System.out.println("[GameClient] Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            
            GameMessage joinMsg = new GameMessage.JoinGameMessage(playerName);
            System.out.println("[GameClient] Sending join message for player: " + playerName);
            sendMessage(joinMsg);
            
            // Start listening for server messages
            Thread listenerThread = new Thread(this::listenForMessages);
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    private void listenForMessages() {
        try {
            while (connected) {
                GameMessage message = (GameMessage) in.readObject();
                handleServerMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("Server disconnected");
            connected = false;
        } catch (ClassNotFoundException | IOException e) {
            if (connected) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }
    }

    private void handleServerMessage(GameMessage message) {
        System.out.println("[GameClient] Received message type: " + message.getType());
        switch (message.getType()) {
            case WAITING:
                if (message instanceof GameMessage.TextMessage) {
                    System.out.println("\n" + ((GameMessage.TextMessage) message).getMessage());
                }
                break;
            case YOUR_TURN:
                myTurn = true;
                System.out.println("[GameClient] It's our turn!");
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    System.out.println("\n" + stateMsg.getMessage());
                    if (stateMsg.getBoardState() != null) {
                        updateBoardState(stateMsg.getBoardState());
                        displayBoard();
                        handlePlayerMove();
                    }
                }
                break;
            case OPPONENT_TURN:
                myTurn = false;
                System.out.println("[GameClient] Opponent's turn");
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    System.out.println("\n" + stateMsg.getMessage());
                    if (stateMsg.getBoardState() != null) {
                        updateBoardState(stateMsg.getBoardState());
                        displayBoard();
                    }
                }
                break;
            case OPPONENT_MOVE:
                System.out.println("[GameClient] Opponent made a move");
                if (message instanceof GameMessage.OpponentMoveMessage) {
                    GameMessage.OpponentMoveMessage moveMsg = (GameMessage.OpponentMoveMessage) message;
                    System.out.println("\n" + moveMsg.getMessage());
                    if (moveMsg.getBoardState() != null) {
                        updateBoardState(moveMsg.getBoardState());
                        displayBoard();
                        myTurn = true;
                        handlePlayerMove();
                    }
                }
                break;
            case OPPONENT_PASS:
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    System.out.println("\n" + stateMsg.getMessage());
                    if (stateMsg.getBoardState() != null) {
                        updateBoardState(stateMsg.getBoardState());
                        displayBoard();
                        myTurn = true;
                        handlePlayerMove();
                    }
                }
                break;
            case MOVE_RESPONSE:
                if (message instanceof GameMessage.MoveResponseMessage) {
                    GameMessage.MoveResponseMessage respMsg = (GameMessage.MoveResponseMessage) message;
                    if (respMsg.isSuccess()) {
                        System.out.println("✓ " + respMsg.getMessage());
                    } else {
                        System.out.println("✗ " + respMsg.getMessage());
                        if (myTurn) {
                            handlePlayerMove();
                        }
                    }
                    if (respMsg.getBoardState() != null) {
                        updateBoardState(respMsg.getBoardState());
                        displayBoard();
                    }
                }
                break;
            case GAME_OVER:
                gameActive = false;
                System.out.println("\n========== GAME OVER ==========");
                if (message instanceof GameMessage.TextMessage) {
                    System.out.println(((GameMessage.TextMessage) message).getMessage());
                }
                break;
            case ERROR:
                if (message instanceof GameMessage.TextMessage) {
                    System.out.println("ERROR: " + ((GameMessage.TextMessage) message).getMessage());
                }
                break;
            default:
                System.out.println("Unknown message type: " + message.getType());
        }
    }

    private void updateBoardState(int[][] boardState) {
        for (int r = 0; r < boardState.length; r++) {
            for (int c = 0; c < boardState[r].length; c++) {
                Intersection inter = board.getIntersection(r, c);
                if (boardState[r][c] == 0) {
                    // Empty, do nothing
                } else if (boardState[r][c] == 1) {
                    // Black stone
                    inter.setStone(new Stone(Stone.Color.BLACK));
                } else if (boardState[r][c] == 2) {
                    // White stone
                    inter.setStone(new Stone(Stone.Color.WHITE));
                }
            }
        }
    }

    private void displayBoard() {
        System.out.println(renderer.render(board));
    }

    private void handlePlayerMove() {
        while (myTurn) {
            System.out.print("Your move (e.g., D4), 'pass', or 'resign': ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("pass")) {
                GameMessage msg = new GameMessage.SimpleMessage(GameMessage.MessageType.PASS);
                sendMessage(msg);
                myTurn = false;
                break;
            } else if (input.equals("resign")) {
                GameMessage msg = new GameMessage.SimpleMessage(GameMessage.MessageType.RESIGN);
                sendMessage(msg);
                myTurn = false;
                gameActive = false;
                break;
            } else {
                int[] pos = parseMove(input);
                if (pos != null) {
                    GameMessage msg = new GameMessage.MoveMessage(
                        GameMessage.MessageType.MOVE, pos[0], pos[1]);
                    sendMessage(msg);
                    myTurn = false;
                    break;
                } else {
                    System.out.println("Invalid format. Use coordinates like D4, A1, etc.");
                }
            }
        }
    }

    private int[] parseMove(String input) {
        if (input.length() < 2) return null;

        char colChar = Character.toUpperCase(input.charAt(0));
        String rowStr = input.substring(1).trim();

        // Valid letters A-H, J-T (skip I)
        if (colChar < 'A' || colChar > 'T' || colChar == 'I') {
            return null;
        }

        int col = colChar - 'A';
        if (colChar > 'I') {
            // Skip the 'I' column
            col -= 1;
        }

        int row;
        try {
            row = Integer.parseInt(rowStr) - 1; // convert to 0-based
        } catch (NumberFormatException e) {
            return null;
        }

        // Bounds check for 19x19
        if (row < 0 || row >= 19 || col < 0 || col >= 19) {
            return null;
        }

        return new int[]{row, col};
    }

    public void startGame(String opponentName) {
        GameMessage msg = new GameMessage.TextMessage(
            GameMessage.MessageType.START_GAME, opponentName);
        sendMessage(msg);
        gameActive = true;
    }

    public void sendMessage(GameMessage message) {
        try {
            synchronized (out) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public static void main(String[] args) {
        System.out.println("=== GO Game Client ===");
        System.out.print("Enter your name: ");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine().trim();
        
        GameClient client = new GameClient(name);
        
        if (!client.connect()) {
            System.out.println("Failed to connect to server");
            return;
        }
        
        System.out.println("\nWaiting for game to start...");
        System.out.println("Your player name: " + name);
        System.out.println("Color will be assigned randomly.");
        
        // Keep the client running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
