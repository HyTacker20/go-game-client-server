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
 *
 * Internally, the factory uses BoardBuilder,
 * which allows the board configuration to be extended
 * in the future without changing the factory API.
 */
public class BoardFactory {

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
     *
     * This method allows flexibility while still keeping
     * board creation logic in one place.
     */
    public static Board custom(int size) {
        return new BoardBuilder()
                .size(size)
                .build();
    }
}
