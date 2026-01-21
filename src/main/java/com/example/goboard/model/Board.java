package com.example.goboard.model;

/**
 * Represents a Go board.
 *
 * Delegates to specialized components:
 * - CaptureEngine: handles captures, liberties, and group collection
 * - ScoreCalculator: calculates territory and scores
 * - RuleEnforcer: enforces ko, superko, and suicide rules
 * - SekiDetector: detects and marks seki situations
 *
 * Responsibilities:
 * - Maintain the board grid and intersections
 * - Coordinate stone placement
 * - Track prisoner counts
 * - Provide access to board state
 */
public class Board {

    private final int size;
    private final Intersection[][] intersections;

    // Prisoners captured during the game
    private int blackPrisoners = 0;
    private int whitePrisoners = 0;

    // Delegate components
    private final CaptureEngine captureEngine;
    private final ScoreCalculator scoreCalculator;
    private final RuleEnforcer ruleEnforcer;
    private final SekiDetector sekiDetector;

    public Board(int size) {
        if (size <= 0) throw new IllegalArgumentException();
        this.size = size;

        intersections = new Intersection[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                intersections[r][c] = new Intersection(r, c);

        // Initialize delegate components
        this.captureEngine = new CaptureEngine(this);
        this.scoreCalculator = new ScoreCalculator(this);
        this.ruleEnforcer = new RuleEnforcer(this);
        this.sekiDetector = new SekiDetector(this, captureEngine);
    }

    /* ======================================================
       BASIC ACCESS
       ====================================================== */

    public int getSize() {
        return size;
    }

    public Intersection getIntersection(int r, int c) {
        if (r < 0 || c < 0 || r >= size || c >= size) return null;
        return intersections[r][c];
    }

    /**
     * Get the number of white stones captured by black.
     */
    public int getBlackPrisoners() {
        return blackPrisoners;
    }

    /**
     * Get the number of black stones captured by white.
     */
    public int getWhitePrisoners() {
        return whitePrisoners;
    }

    public void incrementBlackPrisoners(int delta) {
        blackPrisoners += delta;
    }

    public void incrementWhitePrisoners(int delta) {
        whitePrisoners += delta;
    }

    /* ======================================================
       MOVE LOGIC
       ====================================================== */

    /**
     * Attempt to place a stone on the board.
     * 
     * @param row row coordinate
     * @param col column coordinate
     * @param stone stone to place
     * @return number of captured stones, or -1 if move is illegal
     */
    public int placeStone(int row, int col, Stone stone) {
        Intersection it = getIntersection(row, col);
        if (it == null || !it.isEmpty()) return -1;

        // Snapshot board state before the move
        Stone.Color[][] before = ruleEnforcer.snapshotBoardState();
        it.setStone(stone);

        // Process captures
        int capturedThisMove = captureEngine.processCapturesAfterMove(row, col, stone.getColor());
        
        // Update prisoner count
        if (capturedThisMove > 0) {
            if (stone.getColor() == Stone.Color.BLACK) {
                blackPrisoners += capturedThisMove;
            } else {
                whitePrisoners += capturedThisMove;
            }
        }

        // Check suicide rule
        if (captureEngine.countGroupLiberties(row, col) == 0 && capturedThisMove == 0) {
            it.setStone(null);
            return -1;
        }

        // Check superko rule
        Stone.Color[][] after = ruleEnforcer.snapshotBoardState();
        if (ruleEnforcer.violatesSuperko(after)) {
            ruleEnforcer.restoreBoardState(before);
            return -1;
        }

        // Valid move - record position
        ruleEnforcer.recordPosition(after);
        return capturedThisMove;
    }

    /* ======================================================
       JAPANESE SCORING
       ====================================================== */

    /**
     * Count prisoners for a color (delegates to ScoreCalculator).
     */
    public int countStones(Stone.Color color) {
        return scoreCalculator.countPrisoners(color, blackPrisoners, whitePrisoners);
    }

    /**
     * Count territory for a color (delegates to ScoreCalculator).
     */
    public int countTerritory(Stone.Color color) {
        return scoreCalculator.countTerritory(color);
    }

    /**
     * Calculate final score for a color (delegates to ScoreCalculator).
     */
    public double calculateScore(Stone.Color color) {
        return scoreCalculator.calculateScore(color, blackPrisoners, whitePrisoners);
    }

    /* ======================================================
       SEKI DETECTION
       ====================================================== */

    /**
     * Detect and mark seki regions on the board (delegates to SekiDetector).
     */
    public void detectSeki() {
        sekiDetector.detectAndMarkSeki();
    }

    /* ======================================================
       SERIALIZATION
       ====================================================== */

    /**
     * Serialize the board to a 2D integer array.
     * 0 = empty, 1 = black, 2 = white
     * 
     * @return 2D array representation of the board
     */
    public int[][] toIntArray() {
        int[][] state = new int[size][size];
        
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Intersection inter = getIntersection(r, c);
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
}
