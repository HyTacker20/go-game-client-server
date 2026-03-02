package com.example.goboard.javafx;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;
import com.example.goboard.network.GameMessage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * JavaFX application for the Go game client.
 * 
 * Features:
 * - Visual board with clickable intersections
 * - Pass and Resign buttons
 * - Connection status indicator
 * - Current player and turn indicator
 * - Captured stones counter
 * - Server message display area
 * - Network integration with GameClient protocol
 */
public class JavaFXGameClient extends Application {
    
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    private static final double BOARD_SIZE = 700.0;
    
    // Network components
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean connected = false;
    private volatile boolean myTurn = false;
    
    // Game state
    private String playerName;
    private String playerColor;
    private Board board;
    private int blackCaptured = 0;
    private int whiteCaptured = 0;
    
    // UI components
    private BoardView boardView;
    private Label statusLabel;
    private Label turnLabel;
    private Label capturedLabel;
    private Label connectionLabel;
    private Label playerColorLabel;
    private TextArea messageArea;
    private Button passButton;
    private Button resignButton;
    private Button submitDeadButton;
    
    // Input queue for synchronizing UI thread with network thread
    private BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

    // Scoring state
    private boolean scoringPhase = false;
    private Alert activeScoreDialog = null;
    
    @Override
    public void start(Stage primaryStage) {
        // Get player name and color
        if (!showConnectionDialog()) {
            Platform.exit();
            return;
        }
        
        // Create initial board
        board = new Board(19);
        
        // Build UI
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Center: Board view
        boardView = new BoardView(board, BOARD_SIZE);
        boardView.setOnIntersectionClick(this::handleIntersectionClick);
        StackPane boardContainer = new StackPane(boardView);
        boardContainer.setPadding(new Insets(10));
        root.setCenter(boardContainer);
        
        // Right: Control panel
        VBox controlPanel = createControlPanel();
        root.setRight(controlPanel);
        
        // Bottom: Message area
        VBox messagePanel = createMessagePanel();
        root.setBottom(messagePanel);
        
        // Create scene and show
        Scene scene = new Scene(root, 1100, 800);
        primaryStage.setTitle("Go Game - " + playerName);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> disconnect());
        primaryStage.show();
        
        // Connect to server
        connectToServer();
    }
    
    private boolean showConnectionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Connect to Game Server");
        dialog.setHeaderText("Enter your player information");
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Player name");
        
        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll("BLACK", "WHITE", "RANDOM");
        colorCombo.setValue("RANDOM");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorCombo, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Focus on name field
        Platform.runLater(nameField::requestFocus);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            playerName = nameField.getText().trim();
            playerColor = colorCombo.getValue();
            if (playerName.isEmpty()) {
                playerName = "Player";
            }
            return true;
        }
        return false;
    }
    
    private VBox createControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(250);
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        // Title
        Label titleLabel = new Label("Game Controls");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Connection status
        connectionLabel = new Label("Disconnected");
        connectionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        connectionLabel.setTextFill(Color.RED);
        
        // Player color indicator
        playerColorLabel = new Label("Color: Not assigned");
        playerColorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        playerColorLabel.setTextFill(Color.GRAY);
        
        Separator sep1 = new Separator();
        
        // Turn indicator
        turnLabel = new Label("Waiting...");
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        turnLabel.setWrapText(true);
        
        // Status label
        statusLabel = new Label("Not connected");
        statusLabel.setWrapText(true);
        statusLabel.setFont(Font.font("Arial", 12));
        
        Separator sep3 = new Separator();
        
        // Captured stones counter
        capturedLabel = new Label("Captured:\nBlack: 0\nWhite: 0");
        capturedLabel.setFont(Font.font("Arial", 12));
        

        Separator sep4 = new Separator();
        
        // Action buttons
        passButton = new Button("Pass");
        passButton.setPrefWidth(200);
        passButton.setDisable(true);
        passButton.setOnAction(e -> handlePass());
        
        resignButton = new Button("Resign");
        resignButton.setPrefWidth(200);
        resignButton.setDisable(true);
        resignButton.setOnAction(e -> handleResign());

        submitDeadButton = new Button("Submit Dead Stones");
        submitDeadButton.setPrefWidth(200);
        submitDeadButton.setDisable(true);
        submitDeadButton.setOnAction(e -> handleSubmitDeadStones());
        
        panel.getChildren().addAll(
            titleLabel,
            connectionLabel,
            playerColorLabel,
            sep1,
            turnLabel,
            statusLabel,
            sep3,
            capturedLabel,
            sep4,
            passButton,
            resignButton,
            submitDeadButton
        );
        
        return panel;
    }
    
    private VBox createMessagePanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        
        Label messageLabel = new Label("Server Messages:");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefHeight(100);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-control-inner-background: #f9f9f9;");
        
        panel.getChildren().addAll(messageLabel, messageArea);
        return panel;
    }
    
    private void connectToServer() {
        new Thread(() -> {
            try {
                appendMessage("Connecting to " + SERVER_HOST + ":" + SERVER_PORT + "...");
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                connected = true;
                
                Platform.runLater(() -> {
                    connectionLabel.setText("Connected");
                    connectionLabel.setTextFill(Color.GREEN);
                    appendMessage("Connected successfully as " + playerName);
                });
                
                // Send join message
                GameMessage joinMsg = new GameMessage.JoinGameMessage(playerName);
                sendMessage(joinMsg);
                
                // Start listening for server messages
                listenForMessages();
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    appendMessage("Failed to connect: " + e.getMessage());
                    showError("Connection Failed", "Could not connect to server: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void listenForMessages() {
        try {
            while (connected) {
                GameMessage message = (GameMessage) in.readObject();
                GameMessage finalMessage = message;
                Platform.runLater(() -> handleServerMessage(finalMessage));
            }
        } catch (EOFException e) {
            Platform.runLater(() -> {
                appendMessage("Server closed connection");
                disconnect();
            });
        } catch (ClassNotFoundException | IOException e) {
            if (connected) {
                Platform.runLater(() -> appendMessage("Error receiving message: " + e.getMessage()));
            }
        }
    }
    
    private void handleServerMessage(GameMessage message) {
        switch (message.getType()) {
            case WAITING:
                if (message instanceof GameMessage.TextMessage) {
                    String msg = ((GameMessage.TextMessage) message).getMessage();
                    appendMessage(msg);
                    statusLabel.setText(msg);
                }
                break;

            case SCORING_PHASE:
                scoringPhase = true;
                myTurn = false;
                passButton.setDisable(true);
                resignButton.setDisable(true);
                submitDeadButton.setDisable(false);
                statusLabel.setText("Scoring phase: mark dead stones and submit");
                appendMessage("Scoring phase started. Mark dead stones and press Submit.");
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    updateBoardState(stateMsg);
                    updateCapturedFromMessage(stateMsg.getBlackCaptured(), stateMsg.getWhiteCaptured());
                }
                boardView.clearDeadStones();
                boardView.setScoringMode(true);
                break;
                
            case YOUR_TURN:
                // Close score dialog if opponent rejected
                if (activeScoreDialog != null) {
                    activeScoreDialog.close();
                    activeScoreDialog = null;
                }
                scoringPhase = false;
                submitDeadButton.setDisable(true);
                boardView.setScoringMode(false);
                boardView.clearDeadStones();
                myTurn = true;
                turnLabel.setText("YOUR TURN");
                turnLabel.setTextFill(Color.GREEN);
                passButton.setDisable(false);
                resignButton.setDisable(false);
                statusLabel.setText("Make your move");
                
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    updateBoardState(stateMsg);
                    updateCapturedFromMessage(stateMsg.getBlackCaptured(), stateMsg.getWhiteCaptured());
                    updatePlayerColorFromState(stateMsg);
                }
                break;
                
            case OPPONENT_TURN:
                // Close score dialog if opponent rejected
                if (activeScoreDialog != null) {
                    activeScoreDialog.close();
                    activeScoreDialog = null;
                }
                scoringPhase = false;
                submitDeadButton.setDisable(true);
                boardView.setScoringMode(false);
                boardView.clearDeadStones();
                myTurn = false;
                turnLabel.setText("Opponent's Turn");
                turnLabel.setTextFill(Color.ORANGE);
                passButton.setDisable(true);
                resignButton.setDisable(true);
                statusLabel.setText("Waiting for opponent...");
                
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    updateBoardState(stateMsg);
                    updateCapturedFromMessage(stateMsg.getBlackCaptured(), stateMsg.getWhiteCaptured());
                    updatePlayerColorFromState(stateMsg);
                }
                break;
                
            case OPPONENT_MOVE:
                if (message instanceof GameMessage.OpponentMoveMessage) {
                    GameMessage.OpponentMoveMessage moveMsg = (GameMessage.OpponentMoveMessage) message;
                    String pos = formatPosition(moveMsg.getRow(), moveMsg.getCol());
                    appendMessage("Opponent played at " + pos);
                    
                    if (moveMsg.getBoardState() != null) {
                        updateBoardState(moveMsg.getBoardState());
                    }
                    updateCapturedFromMessage(moveMsg.getBlackCaptured(), moveMsg.getWhiteCaptured());
                    
                    myTurn = true;
                    turnLabel.setText("YOUR TURN");
                    turnLabel.setTextFill(Color.GREEN);
                    passButton.setDisable(false);
                    resignButton.setDisable(false);
                }
                break;
                
            case OPPONENT_PASS:
                appendMessage("Opponent passed");
                if (message instanceof GameMessage.BoardStateMessage) {
                    GameMessage.BoardStateMessage stateMsg = (GameMessage.BoardStateMessage) message;
                    updateBoardState(stateMsg);
                    updateCapturedFromMessage(stateMsg.getBlackCaptured(), stateMsg.getWhiteCaptured());
                }
                myTurn = true;
                turnLabel.setText("YOUR TURN");
                turnLabel.setTextFill(Color.GREEN);
                passButton.setDisable(false);
                resignButton.setDisable(false);
                break;
                
            case MOVE_RESPONSE:
                if (message instanceof GameMessage.MoveResponseMessage) {
                    GameMessage.MoveResponseMessage respMsg = (GameMessage.MoveResponseMessage) message;
                    if (respMsg.isSuccess()) {
                        appendMessage("Move accepted");
                        if (respMsg.getBoardState() != null) {
                            updateBoardState(respMsg.getBoardState());
                        }
                        updateCapturedFromMessage(respMsg.getBlackCaptured(), respMsg.getWhiteCaptured());
                        myTurn = false;
                        turnLabel.setText("Opponent's Turn");
                        turnLabel.setTextFill(Color.ORANGE);
                        statusLabel.setText("Waiting for opponent...");
                        passButton.setDisable(true);
                        resignButton.setDisable(true);
                    } else {
                        appendMessage("Invalid move: " + respMsg.getMessage());
                        showError("Invalid Move", respMsg.getMessage());
                        // Move was rejected - restore turn and re-enable buttons
                        myTurn = true;
                        turnLabel.setText("YOUR TURN");
                        turnLabel.setTextFill(Color.GREEN);
                        statusLabel.setText("Make your move");
                        passButton.setDisable(false);
                        resignButton.setDisable(false);
                    }
                }
                break;
                
            case GAME_OVER:
                if (message instanceof GameMessage.TextMessage) {
                    String result = ((GameMessage.TextMessage) message).getMessage();
                    appendMessage("Game Over: " + result);
                    showInfo("Game Over", result);
                }
                passButton.setDisable(true);
                resignButton.setDisable(true);
                myTurn = false;
                scoringPhase = false;
                submitDeadButton.setDisable(true);
                boardView.setScoringMode(false);
                break;
            
            case SCORE_RESPONSE:
                if (message instanceof GameMessage.ScoreMessage) {
                    GameMessage.ScoreMessage scoreMsg = (GameMessage.ScoreMessage) message;
                    handleScoringPhase(scoreMsg);
                }
                break;
                
            case ERROR:
                if (message instanceof GameMessage.TextMessage) {
                    String error = ((GameMessage.TextMessage) message).getMessage();
                    appendMessage("Error: " + error);
                    showError("Error", error);
                }
                break;
                
            default:
                // Ignore unknown messages
        }
    }
    
    private void updateBoardState(GameMessage.BoardStateMessage stateMsg) {
        int[][] boardState = stateMsg.getBoardState();
        if (boardState != null) {
            updateBoardState(boardState);
        }
    }
    
    private void updateBoardState(int[][] boardState) {
        // Check if board size matches - if not, recreate board and view
        if (boardState.length != board.getSize()) {
            int newSize = boardState.length;
            appendMessage("Server using " + newSize + "x" + newSize + " board");
            board = new Board(newSize);
            boardView.setBoard(board);
        }
        
        for (int r = 0; r < boardState.length; r++) {
            for (int c = 0; c < boardState[r].length; c++) {
                Intersection inter = board.getIntersection(r, c);
                if (inter != null) {
                    if (boardState[r][c] == 0) {
                        inter.setStone(null);
                    } else if (boardState[r][c] == 1) {
                        inter.setStone(new Stone(Stone.Color.BLACK));
                    } else if (boardState[r][c] == 2) {
                        inter.setStone(new Stone(Stone.Color.WHITE));
                    }
                }
            }
        }
        boardView.render();
        updateCapturedCount();
    }
    
    private void updateCapturedCount() {
        blackCaptured = board.getBlackPrisoners();
        whiteCaptured = board.getWhitePrisoners();
        capturedLabel.setText(String.format("Captured:\nBlack: %d\nWhite: %d", 
                                           blackCaptured, whiteCaptured));
    }
    
    private void updateCapturedFromMessage(int black, int white) {
        blackCaptured = black;
        whiteCaptured = white;
        capturedLabel.setText(String.format("Captured:\nBlack: %d\nWhite: %d", 
                                           blackCaptured, whiteCaptured));
    }
    
    private void updatePlayerColorFromState(GameMessage.BoardStateMessage stateMsg) {
        if (playerColorLabel.getText().equals("Color: Not assigned")) {
            // Determine color based on whether it's our turn at game start
            // Black always plays first, so if it's our turn, we are BLACK
            if (myTurn) {
                playerColorLabel.setText("Color: BLACK");
                playerColorLabel.setTextFill(Color.BLACK);
            } else {
                playerColorLabel.setText("Color: WHITE");
                playerColorLabel.setTextFill(Color.DARKGRAY);
            }
        }
    }
    
    private void handleScoringPhase(GameMessage.ScoreMessage scoreMsg) {
        scoringPhase = true;
        // Disable normal game controls during scoring
        passButton.setDisable(true);
        resignButton.setDisable(true);
        submitDeadButton.setDisable(true);
        myTurn = false;
        
        // Update board state if provided
        if (scoreMsg.getBoardState() != null) {
            updateBoardState(scoreMsg.getBoardState());
        }
        
        boardView.setScoringMode(true);
        
        double blackScore = scoreMsg.getBlackScore();
        double whiteScore = scoreMsg.getWhiteScore();
        
        appendMessage("Scoring Phase Started");
        appendMessage("Score: " + scoreMsg.getBlackPlayerName() + " = " + blackScore + 
                     ", " + scoreMsg.getWhitePlayerName() + " = " + whiteScore);
        
        // Show dialog asking for score confirmation (non-blocking)
        Alert scoreDialog = new Alert(Alert.AlertType.CONFIRMATION);
        activeScoreDialog = scoreDialog;
        scoreDialog.setTitle("Score Confirmation");
        scoreDialog.setHeaderText("Game Scoring");
        scoreDialog.setContentText(
            scoreMsg.getBlackPlayerName() + " (Black): " + blackScore + " points\n" +
            scoreMsg.getWhitePlayerName() + " (White): " + whiteScore + " points\n\n" +
            "Do you accept this score?");
        
        scoreDialog.getButtonTypes().setAll(
            new ButtonType("Accept", ButtonBar.ButtonData.YES),
            new ButtonType("Reject", ButtonBar.ButtonData.NO)
        );
        
        // Make dialog non-blocking to allow processing opponent's rejection
        scoreDialog.setOnHidden(event -> {
            activeScoreDialog = null;
            ButtonType result = scoreDialog.getResult();
            
            // Only send confirmation if dialog was not forcibly closed
            if (result != null && scoringPhase) {
                boolean accepted = result.getButtonData() == ButtonBar.ButtonData.YES;
                
                // Send confirmation to server
                GameMessage confirmMsg = new GameMessage.ScoreConfirmationMessage(accepted);
                sendMessage(confirmMsg);
                
                boardView.setScoringMode(false);
                scoringPhase = false;
                
                if (accepted) {
                    appendMessage("You accepted the score. Waiting for opponent...");
                    statusLabel.setText("Waiting for opponent to confirm score...");
                } else {
                    appendMessage("You rejected the score. Game continues...");
                }
            }
        });
        
        scoreDialog.show();
    }
    
    private void handleIntersectionClick(int row, int col) {
        if (scoringPhase) {
            boardView.toggleDeadStone(row, col);
            return;
        }

        if (!myTurn) {
            appendMessage("Wait for your turn!");
            return;
        }
        
        Intersection inter = board.getIntersection(row, col);
        if (inter == null || !inter.isEmpty()) {
            appendMessage("Invalid position - already occupied");
            return;
        }
        
        // Send move to server
        String position = formatPosition(row, col);
        appendMessage("Playing at " + position);
        
        GameMessage moveMsg = new GameMessage.MoveMessage(GameMessage.MessageType.MOVE, row, col);
        sendMessage(moveMsg);
        
        // Temporarily disable buttons while waiting for server response
        // Don't set myTurn = false yet, in case the move is rejected
        passButton.setDisable(true);
        resignButton.setDisable(true);
    }

    /**
     * Submit marked dead stones to server during scoring phase.
     */
    private void handleSubmitDeadStones() {
        if (!scoringPhase) {
            return;
        }

        var dead = boardView.getDeadStones();
        int[][] coords = new int[dead.size()][2];
        int i = 0;
        for (String s : dead) {
            String[] parts = s.split(",");
            if (parts.length == 2) {
                coords[i][0] = Integer.parseInt(parts[0]);
                coords[i][1] = Integer.parseInt(parts[1]);
                i++;
            }
        }
        if (i < coords.length) {
            int[][] trimmed = new int[i][2];
            System.arraycopy(coords, 0, trimmed, 0, i);
            coords = trimmed;
        }

        GameMessage deadMsg = new GameMessage.DeadStonesMessage(coords);
        sendMessage(deadMsg);

        statusLabel.setText("Dead stones submitted. Waiting for score...");
        appendMessage("Submitted dead stones: " + coords.length);
        submitDeadButton.setDisable(true);
    }
    
    private void handlePass() {
        if (!myTurn) {
            return;
        }
        
        appendMessage("You passed");
        GameMessage passMsg = new GameMessage.SimpleMessage(GameMessage.MessageType.PASS);
        sendMessage(passMsg);
        
        myTurn = false;
        passButton.setDisable(true);
        resignButton.setDisable(true);
    }
    
    private void handleResign() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resign");
        alert.setHeaderText("Are you sure you want to resign?");
        alert.setContentText("This will end the game.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            appendMessage("You resigned");
            GameMessage resignMsg = new GameMessage.SimpleMessage(GameMessage.MessageType.RESIGN);
            sendMessage(resignMsg);
            
            myTurn = false;
            passButton.setDisable(true);
            resignButton.setDisable(true);
        }
    }
    
    private void sendMessage(GameMessage message) {
        if (!connected || out == null) {
            appendMessage("Not connected to server");
            return;
        }
        
        new Thread(() -> {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                Platform.runLater(() -> 
                    appendMessage("Error sending message: " + e.getMessage())
                );
            }
        }).start();
    }
    
    private void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        
        Platform.runLater(() -> {
            connectionLabel.setText("Disconnected");
            connectionLabel.setTextFill(Color.RED);
            appendMessage("Disconnected from server");
        });
    }
    
    private void appendMessage(String message) {
        messageArea.appendText(message + "\n");
        messageArea.setScrollTop(Double.MAX_VALUE);
    }
    
    private String formatPosition(int row, int col) {
        String columns = "ABCDEFGHJKLMNOPQRST";
        return columns.charAt(col) + String.valueOf(board.getSize() - row);
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
