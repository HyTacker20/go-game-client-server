package com.example.goboard.network;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;
import com.example.goboard.view.AsciiBoardRenderer;
import com.example.goboard.view.ConsoleUIFormatter;

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
    private volatile boolean connected = false;
    private volatile boolean gameActive = false;
    private volatile boolean myTurn = false;
    private volatile boolean waitingForInput = false;
    private Thread inputThread;
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
            ConsoleUIFormatter.printConnecting(SERVER_HOST, SERVER_PORT);
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            ConsoleUIFormatter.printConnected(SERVER_HOST, SERVER_PORT, playerName);
            
            GameMessage joinMsg = new GameMessage.JoinGameMessage(playerName);
            sendMessage(joinMsg);
            
            // Start listening for server messages
            Thread listenerThread = new Thread(this::listenForMessages);
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            return true;
        } catch (IOException e) {
            ConsoleUIFormatter.printError("Failed to connect to server: " + e.getMessage());
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
            ConsoleUIFormatter.printDisconnected("Server closed connection");
            connected = false;
        } catch (ClassNotFoundException | IOException e) {
            if (connected) {
                ConsoleUIFormatter.printError("Error receiving message: " + e.getMessage());
            }
        }
    }

    private void handleServerMessage(GameMessage message) {
        switch (message.getType()) {
            case WAITING:
                if (message instanceof GameMessage.TextMessage) {
                    ConsoleUIFormatter.printWaiting(((GameMessage.TextMessage) message).getMessage());
                }
                break;
            case YOUR_TURN:
                myTurn = true;
                ConsoleUIFormatter.printTurnInfo(playerName, playerColor, true);
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    if (stateMsg.getBoardState() != null) {
                        updateBoardState(stateMsg.getBoardState());
                        displayBoard();
                        startInputThread();
                    }
                }
                break;
            case OPPONENT_TURN:
                myTurn = false;
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    if (stateMsg.getBoardState() != null) {
                        updateBoardState(stateMsg.getBoardState());
                        displayBoard();
                        ConsoleUIFormatter.printTurnInfo(playerName, playerColor, false);
                        ConsoleUIFormatter.printWaiting("Waiting for opponent's move...");
                    }
                }
                break;
            case OPPONENT_MOVE:
                if (message instanceof GameMessage.OpponentMoveMessage) {
                    GameMessage.OpponentMoveMessage moveMsg = (GameMessage.OpponentMoveMessage) message;
                    String position = formatPosition(moveMsg.getRow(), moveMsg.getCol());
                    ConsoleUIFormatter.printOpponentMove("Opponent", position);
                    if (moveMsg.getBoardState() != null) {
                        updateBoardState(moveMsg.getBoardState());
                        displayBoard();
                        myTurn = true;
                        ConsoleUIFormatter.printTurnInfo(playerName, playerColor, true);
                        startInputThread();
                    }
                }
                break;
            case OPPONENT_PASS:
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    ConsoleUIFormatter.printOpponentPassed("Opponent");
                    if (stateMsg.getBoardState() != null) {
                        updateBoardState(stateMsg.getBoardState());
                        displayBoard();
                        myTurn = true;
                        ConsoleUIFormatter.printTurnInfo(playerName, playerColor, true);
                        startInputThread();
                    }
                }
                break;
            case MOVE_RESPONSE:
                if (message instanceof GameMessage.MoveResponseMessage) {
                    GameMessage.MoveResponseMessage respMsg = (GameMessage.MoveResponseMessage) message;
                    ConsoleUIFormatter.printMoveResponse(respMsg.isSuccess(), respMsg.getMessage());
                    if (respMsg.isSuccess()) {
                        if (respMsg.getBoardState() != null) {
                            updateBoardState(respMsg.getBoardState());
                            displayBoard();
                        }
                    } else {
                        myTurn = true;  // Re-enable turn to allow player to try again
                        startInputThread();
                    }
                }
                break;
            case GAME_OVER:
                gameActive = false;
                if (message instanceof GameMessage.TextMessage) {
                    String result = ((GameMessage.TextMessage) message).getMessage();
                    ConsoleUIFormatter.printGameOver("Game Over", result);
                }
                break;
            case ERROR:
                if (message instanceof GameMessage.TextMessage) {
                    ConsoleUIFormatter.printError(((GameMessage.TextMessage) message).getMessage());
                }
                break;
            default:
                // Silently ignore unknown message types
        }
    }

    private void updateBoardState(int[][] boardState) {
        for (int r = 0; r < boardState.length; r++) {
            for (int c = 0; c < boardState[r].length; c++) {
                Intersection inter = board.getIntersection(r, c);
                if (boardState[r][c] == 0) {
                    // Empty - handles captured stones
                    inter.setStone(null);
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
        ConsoleUIFormatter.clearScreen();
        String boardString = renderer.render(board);
        ConsoleUIFormatter.printBoardWithFrame(boardString);
    }

    private void startInputThread() {
        if (waitingForInput) {
            return; // Already waiting for input
        }
        
        waitingForInput = true;
        inputThread = new Thread(() -> {
            handlePlayerMove();
            waitingForInput = false;
        });
        inputThread.setName("InputThread-" + playerName);
        inputThread.setDaemon(false);
        inputThread.start();
    }

    private void handlePlayerMove() {
        while (myTurn) {
            ConsoleUIFormatter.printMovePrompt();
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
                    ConsoleUIFormatter.printInvalidInput("Use coordinates like D4, A1, etc.");
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

    private String formatPosition(int row, int col) {
        // Convert column number to letter (0->A, 1->B, etc., skipping I)
        char colChar;
        if (col < 8) {
            colChar = (char) ('A' + col);
        } else {
            // Skip 'I' - add 1 to the column
            colChar = (char) ('A' + col + 1);
        }
        
        // Convert row to 1-based number
        int rowNum = row + 1;
        
        return String.valueOf(colChar) + rowNum;
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
            ConsoleUIFormatter.printError("Failed to send message: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        gameActive = false;
        myTurn = false;
        
        // Interrupt input thread if waiting
        if (inputThread != null && inputThread.isAlive()) {
            inputThread.interrupt();
        }
        
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
        ConsoleUIFormatter.printHeader("GO Game - Client");
        System.out.print("Enter your name: ");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine().trim();
        
        if (name.isEmpty()) {
            ConsoleUIFormatter.printError("Name cannot be empty");
            return;
        }
        
        // Enable ANSI support and enter alternative screen buffer
        ConsoleUIFormatter.enableWindowsAnsiSupport();
        ConsoleUIFormatter.enterAlternativeScreen();
        
        GameClient client = new GameClient(name);
        
        if (!client.connect()) {
            ConsoleUIFormatter.exitAlternativeScreen();
            return;
        }
        
        ConsoleUIFormatter.printBlankLine();
        ConsoleUIFormatter.printSectionHeader("Waiting for Game");
        ConsoleUIFormatter.printWaiting("Waiting for an opponent to join...");
        ConsoleUIFormatter.printInfo("Color will be assigned randomly");
        
        // Keep the client running until disconnected
        try {
            while (client.isConnected()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Always restore the original terminal screen on exit
            ConsoleUIFormatter.exitAlternativeScreen();
        }
        
        ConsoleUIFormatter.printInfo("Client disconnected. Exiting...");
    }
}
