package com.example.goboard.model;

/**
 * Represents a single stone placed on the Go board.
 *
 * A stone:
 * - has exactly one color
 * - is immutable (color cannot change after creation)
 *
 * Stones are placed on Intersections and removed when captured.
 */
public class Stone {

    /**
     * Possible stone colors.
     *
     * BLACK and WHITE are normal player stones.
     * UNASSIGNED can be useful as a placeholder or for initialization.
     */
    public enum Color {
        BLACK,
        WHITE,
        UNASSIGNED
    }

    /** Color of this stone */
    private final Color color;

    /**
     * Creates a new stone of the given color.
     *
     * @param color color of the stone
     */
    public Stone(Color color) {
        this.color = color;
    }

    /** @return color of this stone */
    public Color getColor() {
        return color;
    }

    /**
     * Returns a short textual representation of the stone.
     *
     * Used mainly by console board renderers.
     *
     * @return "B" for BLACK, "W" for WHITE, "?" otherwise
     */
    @Override
    public String toString() {
        if (color == Color.BLACK) return "B";
        if (color == Color.WHITE) return "W";
        return "?";
    }
}
