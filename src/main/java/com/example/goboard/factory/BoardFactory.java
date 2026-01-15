package com.example.goboard.factory;

import com.example.goboard.model.Board;
import com.example.goboard.builder.BoardBuilder;

/**
 * BoardFactory is responsible for creating Board instances
 * with predefined or commonly used configurations.
 *
 * This class applies the Factory pattern to:
 * - hide board creation details
 * - provide meaningful, named creation methods
 * - validate board size constraints
 * - offer presets with optimized komi values
 *
 * Internally, the factory uses BoardBuilder,
 * which allows the board configuration to be extended
 * in the future without changing the factory API.
 */
public class BoardFactory {

    // Valid board sizes in Go
    private static final int MIN_BOARD_SIZE = 5;
    private static final int MAX_BOARD_SIZE = 25;
    
    // Standard komi values for different board sizes
    private static final double STANDARD_KOMI_19 = 6.5;  // Tournament standard
    private static final double STANDARD_KOMI_13 = 5.5;  // Common for 13x13
    private static final double STANDARD_KOMI_9 = 5.5;   // Common for 9x9

    /**
     * Creates a standard 19x19 Go board.
     * Used for full-size, tournament-style games.
     * Uses standard tournament komi of 6.5.
     */
    public static Board standard19() {
        return new BoardBuilder()
                .size(19)
                .komi(STANDARD_KOMI_19)
                .build();
    }

    /**
     * Creates a standard 19x19 board with custom komi.
     */
    public static Board standard19(double komi) {
        validateKomi(komi);
        return new BoardBuilder()
                .size(19)
                .komi(komi)
                .build();
    }
    
    /**
     * Creates a medium 13x13 board.
     * Good balance between complexity and game length.
     * Uses komi of 5.5 (common for 13x13).
     */
    public static Board medium13() {
        return new BoardBuilder()
                .size(13)
                .komi(STANDARD_KOMI_13)
                .build();
    }

    /**
     * Creates a 13x13 board with custom komi.
     */
    public static Board medium13(double komi) {
        validateKomi(komi);
        return new BoardBuilder()
                .size(13)
                .komi(komi)
                .build();
    }

    /**
     * Creates a smaller 9x9 board.
     * Useful for beginners, testing, or quick games.
     * Uses komi of 5.5 (common for smaller boards).
     */
    public static Board small9() {
        return new BoardBuilder()
                .size(9)
                .komi(STANDARD_KOMI_9)
                .build();
    }

    /**
     * Creates a 9x9 board with custom komi.
     */
    public static Board small9(double komi) {
        validateKomi(komi);
        return new BoardBuilder()
                .size(9)
                .komi(komi)
                .build();
    }

    /**
     * Creates a board with a custom size.
     * Validates that size is within acceptable range (5-25).
     * Uses default komi of 6.5.
     *
     * @param size board size (must be between 5 and 25)
     * @return new Board
     * @throws IllegalArgumentException if size is invalid
     */
    public static Board custom(int size) {
        validateBoardSize(size);
        return new BoardBuilder()
                .size(size)
                .build();
    }

    /**
     * Creates a board with custom size and komi.
     * Validates both size and komi values.
     * 
     * @param size board size (must be between 5 and 25)
     * @param komi komi value (must be between 0 and 10)
     * @return new Board
     * @throws IllegalArgumentException if size or komi is invalid
     */
    public static Board custom(int size, double komi) {
        validateBoardSize(size);
        validateKomi(komi);
        return new BoardBuilder()
                .size(size)
                .komi(komi)
                .build();
    }
    
    /**
     * Validate board size is within acceptable range.
     * 
     * @param size board size to validate
     * @throws IllegalArgumentException if size is out of range
     */
    private static void validateBoardSize(int size) {
        if (size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
            throw new IllegalArgumentException(
                String.format("Board size must be between %d and %d, got: %d",
                    MIN_BOARD_SIZE, MAX_BOARD_SIZE, size)
            );
        }
    }
    
    /**
     * Validate komi is within reasonable range.
     * 
     * @param komi komi value to validate
     * @throws IllegalArgumentException if komi is invalid
     */
    private static void validateKomi(double komi) {
        if (komi < 0 || komi > 10) {
            throw new IllegalArgumentException(
                String.format("Komi must be between 0 and 10, got: %.1f", komi)
            );
        }
    }
}
