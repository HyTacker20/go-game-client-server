package com.example.goboard.model;


public class Stone {
    public enum Color { BLACK, WHITE }
    private final Color color;


    public Stone(Color color) { this.color = color; }
    public Color getColor() { return color; }
    @Override public String toString() { return color == Color.BLACK ? "B" : "W"; }
}
