package com.example.goboard.javafx;

import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Custom JavaFX control for rendering and interacting with a Go board.
 * 
 * Features:
 * - Renders board grid with star points (hoshi)
 * - Draws black and white stones with gradient effects
 * - Displays coordinate labels
 * - Handles mouse clicks to detect intersection selection
 * - Supports dead stone marking during scoring phase
 */
public class BoardView extends Canvas {
    
    private static final Color BOARD_COLOR = Color.rgb(220, 179, 92); // Wood color
    private static final Color LINE_COLOR = Color.rgb(0, 0, 0, 0.8);
    private static final Color STAR_POINT_COLOR = Color.rgb(0, 0, 0);
    private static final Color COORD_LABEL_COLOR = Color.rgb(60, 60, 60);
    
    private static final double MARGIN = 40.0;
    private static final double STONE_RADIUS_RATIO = 0.45; // Relative to cell size
    
    private Board board;
    private double cellSize;
    private BiConsumer<Integer, Integer> onIntersectionClick;
    private Set<String> deadStones = new HashSet<>();
    private boolean scoringMode = false;
    
    public BoardView(Board board, double size) {
        super(size, size);
        this.board = board;
        this.cellSize = (size - 2 * MARGIN) / (board.getSize() - 1);
        
        // Handle mouse clicks
        setOnMouseClicked(this::handleMouseClick);
        
        // Initial render
        render();
    }
    
    /**
     * Set callback for intersection clicks.
     * @param callback receives (row, col) when an intersection is clicked
     */
    public void setOnIntersectionClick(BiConsumer<Integer, Integer> callback) {
        this.onIntersectionClick = callback;
    }
    
    /**
     * Update the board model and re-render.
     */
    public void setBoard(Board board) {
        this.board = board;
        render();
    }
    
    /**
     * Enable/disable scoring mode for dead stone marking.
     */
    public void setScoringMode(boolean scoringMode) {
        this.scoringMode = scoringMode;
        render();
    }
    
    /**
     * Mark or unmark stones as dead.
     */
    public void toggleDeadStone(int row, int col) {
        String key = row + "," + col;
        if (deadStones.contains(key)) {
            deadStones.remove(key);
        } else {
            deadStones.add(key);
        }
        render();
    }
    
    /**
     * Clear all dead stone markings.
     */
    public void clearDeadStones() {
        deadStones.clear();
        render();
    }
    
    /**
     * Get the set of dead stone positions.
     */
    public Set<String> getDeadStones() {
        return new HashSet<>(deadStones);
    }
    
    /**
     * Main rendering method.
     */
    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        
        // Clear and fill background
        gc.setFill(BOARD_COLOR);
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw grid lines
        drawGrid(gc);
        
        // Draw star points (hoshi)
        drawStarPoints(gc);
        
        // Draw coordinate labels
        drawCoordinateLabels(gc);
        
        // Draw stones
        drawStones(gc);
    }
    
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(LINE_COLOR);
        gc.setLineWidth(1.0);
        
        int size = board.getSize();
        
        // Vertical lines
        for (int i = 0; i < size; i++) {
            double x = MARGIN + i * cellSize;
            gc.strokeLine(x, MARGIN, x, MARGIN + (size - 1) * cellSize);
        }
        
        // Horizontal lines
        for (int i = 0; i < size; i++) {
            double y = MARGIN + i * cellSize;
            gc.strokeLine(MARGIN, y, MARGIN + (size - 1) * cellSize, y);
        }
    }
    
    private void drawStarPoints(GraphicsContext gc) {
        gc.setFill(STAR_POINT_COLOR);
        int size = board.getSize();
        int[][] starPoints = getStarPoints(size);
        
        for (int[] point : starPoints) {
            double x = MARGIN + point[1] * cellSize;
            double y = MARGIN + point[0] * cellSize;
            gc.fillOval(x - 3, y - 3, 6, 6);
        }
    }
    
    private int[][] getStarPoints(int size) {
        if (size == 19) {
            return new int[][] {
                {3, 3}, {3, 9}, {3, 15},
                {9, 3}, {9, 9}, {9, 15},
                {15, 3}, {15, 9}, {15, 15}
            };
        } else if (size == 13) {
            return new int[][] {
                {3, 3}, {3, 9},
                {6, 6},
                {9, 3}, {9, 9}
            };
        } else if (size == 9) {
            return new int[][] {
                {2, 2}, {2, 6},
                {4, 4},
                {6, 2}, {6, 6}
            };
        }
        return new int[0][0];
    }
    
    private void drawCoordinateLabels(GraphicsContext gc) {
        gc.setFill(COORD_LABEL_COLOR);
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        
        int size = board.getSize();
        String columns = "ABCDEFGHJKLMNOPQRST"; // Skip 'I'
        
        // Column labels (top and bottom)
        for (int i = 0; i < size; i++) {
            double x = MARGIN + i * cellSize;
            gc.fillText(String.valueOf(columns.charAt(i)), x, MARGIN - 15);
            gc.fillText(String.valueOf(columns.charAt(i)), x, MARGIN + (size - 1) * cellSize + 25);
        }
        
        // Row labels (left and right)
        for (int i = 0; i < size; i++) {
            double y = MARGIN + i * cellSize;
            String label = String.valueOf(size - i);
            gc.fillText(label, MARGIN - 20, y + 5);
            gc.fillText(label, MARGIN + (size - 1) * cellSize + 20, y + 5);
        }
    }
    
    private void drawStones(GraphicsContext gc) {
        int size = board.getSize();
        double radius = cellSize * STONE_RADIUS_RATIO;
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Intersection inter = board.getIntersection(row, col);
                if (inter != null && !inter.isEmpty()) {
                    Stone stone = inter.getStone();
                    double x = MARGIN + col * cellSize;
                    double y = MARGIN + row * cellSize;
                    
                    boolean isDead = deadStones.contains(row + "," + col);
                    drawStone(gc, x, y, radius, stone.getColor(), isDead);
                }
            }
        }
    }
    
    private void drawStone(GraphicsContext gc, double x, double y, double radius, 
                          Stone.Color color, boolean isDead) {
        // Draw shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(x - radius + 2, y - radius + 2, radius * 2, radius * 2);
        
        // Draw stone with gradient
        if (color == Stone.Color.BLACK) {
            RadialGradient gradient = new RadialGradient(
                0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(80, 80, 80)),
                new Stop(1, Color.rgb(20, 20, 20))
            );
            gc.setFill(gradient);
        } else {
            RadialGradient gradient = new RadialGradient(
                0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(1, Color.rgb(200, 200, 200))
            );
            gc.setFill(gradient);
        }
        
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // Draw border
        gc.setStroke(color == Stone.Color.BLACK ? Color.rgb(40, 40, 40) : Color.rgb(180, 180, 180));
        gc.setLineWidth(1.0);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // Mark dead stones with X
        if (isDead) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2.0);
            double offset = radius * 0.6;
            gc.strokeLine(x - offset, y - offset, x + offset, y + offset);
            gc.strokeLine(x - offset, y + offset, x + offset, y - offset);
        }
    }
    
    private void handleMouseClick(MouseEvent event) {
        if (onIntersectionClick == null) {
            return;
        }
        
        double x = event.getX();
        double y = event.getY();
        
        // Convert pixel coordinates to board coordinates
        int col = (int) Math.round((x - MARGIN) / cellSize);
        int row = (int) Math.round((y - MARGIN) / cellSize);
        
        // Validate coordinates
        if (row >= 0 && row < board.getSize() && col >= 0 && col < board.getSize()) {
            onIntersectionClick.accept(row, col);
        }
    }
    
    /**
     * Highlight the last move (optional feature).
     */
    public void highlightLastMove(int row, int col) {
        GraphicsContext gc = getGraphicsContext2D();
        double x = MARGIN + col * cellSize;
        double y = MARGIN + row * cellSize;
        
        gc.setStroke(Color.RED);
        gc.setLineWidth(2.0);
        double size = cellSize * 0.3;
        gc.strokeRect(x - size / 2, y - size / 2, size, size);
    }
}
