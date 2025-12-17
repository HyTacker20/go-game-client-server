package com.example.goboard.builder;

import com.example.goboard.model.Board;

/**
 * Builder class used to create Board instances.
 *
 * Allows flexible configuration of the board before creation
 * (e.g. different board sizes: 9x9, 13x13, 19x19).
 *
 * Example usage:
 * Board board = new BoardBuilder()
 *                  .size(9)
 *                  .build();
 */
public class BoardBuilder {

    /** Board size (number of rows and columns) */
    private int size;

    /**
     * Sets the board size.
     *
     * @param s board size (must be > 0)
     * @return this builder for method chaining
     */
    public BoardBuilder size(int s) {
        this.size = s;
        return this;
    }

    /**
     * Creates a new Board instance with the configured parameters.
     *
     * @return new Board
     */
    public Board build() {
        return new Board(size);
    }
}
