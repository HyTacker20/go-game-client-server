package com.example.goboard.model;

/**
 * Represents a single stone placed on the Go board.
 *
 * Responsibilities:
 * - Stores a color (BLACK or WHITE)
 * - Immutable: color cannot be changed after creation
 * - Provides a textual representation for console/UI display
 *
 * Stones are placed on Intersections and may be removed when captured.
 */
public class Stone {

    /**
     * Possible stone colors.
     * - BLACK and WHITE: normal player stones
     * - UNASSIGNED: placeholder or initialization purposes
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
        if (color == null) {
            throw new IllegalArgumentException("Stone color cannot be null");
        }
        this.color = color;
    }

    /** @return color of this stone */
    public Color getColor() {
        return color;
    }

    /**
     * Returns a short textual representation of the stone.
     * Used mainly by console board renderers.
     *
     * @return "B" for BLACK, "W" for WHITE, "?" for UNASSIGNED
     */
    @Override
    public String toString() {
        switch (color) {
            case BLACK: return "B";
            case WHITE: return "W";
            default: return "?";
        }
    }

    /**
     * Compares two stones for equality based on color.
     *
     * @param obj other object to compare
     * @return true if both are Stone instances with the same color
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Stone)) return false;
        Stone other = (Stone) obj;
        return this.color == other.color;
    }

    @Override
    public int hashCode() {
        return color.hashCode();
    }
}
