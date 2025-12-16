package com.example.goboard.model;


public class Stone {
    public enum Color { BLACK, WHITE, UNASSIGNED }
    private final Color color;


    public Stone(Color color) { this.color = color; }
    public Color getColor() { return color; }
    @Override public String toString() {
        if (color == Color.BLACK) return "B";
        if (color == Color.WHITE) return "W";
        return "?";
    }
}
