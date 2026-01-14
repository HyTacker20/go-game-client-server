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

    /** Komi compensation for white (default 6.5) */
    private double komi = 6.5;

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
     * Sets the komi value.
     *
     * @param k komi value (typically 5.5, 6.5, or 7.5)
     * @return this builder for method chaining
     */
    public BoardBuilder komi(double k) {
        this.komi = k;
        return this;
    }

    /**
     * Creates a new Board instance with the configured parameters.
     *
     * @return new Board
     */
    public Board build() {
        return new Board(size, komi);
    }
}
