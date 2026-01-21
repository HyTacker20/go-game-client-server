package com.example.goboard.factory;

import com.example.goboard.builder.BoardBuilder;
import com.example.goboard.model.Board;

/**
 * BoardFactory is responsible for creating Board instances
 * with predefined or commonly used configurations.
 *
 * This class applies the Factory pattern to:
 * - hide board creation details
 * - provide meaningful, named creation methods
 * - validate board size constraints
 *
 * Internally, the factory uses BoardBuilder,
 * which allows the board configuration to be extended
 * in the future without changing the factory API.
 */
public class BoardFactory {

    // Valid board sizes in Go
    private static final int MIN_BOARD_SIZE = 5;
    private static final int MAX_BOARD_SIZE = 25;

    /**
     * Creates a standard 19x19 Go board.
     * Used for full-size, tournament-style games.
     */
    public static Board standard19() {
        return new BoardBuilder()
                .size(19)
                .build();
    }
    
    /**
     * Creates a medium 13x13 board.
     * Good balance between complexity and game length.
     */
    public static Board medium13() {
        return new BoardBuilder()
                .size(13)
                .build();
    }

    /**
     * Creates a smaller 9x9 board.
     * Useful for beginners, testing, or quick games.
     */
    public static Board small9() {
        return new BoardBuilder()
                .size(9)
                .build();
    }

    /**
     * Creates a board with a custom size.
     * Validates that size is within acceptable range (5-25).
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
}
