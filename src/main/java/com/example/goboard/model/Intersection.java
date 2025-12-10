package com.example.goboard.model;


public class Intersection {
    private final int row;
    private final int col;
    private Stone stone;


    public Intersection(int row, int col) {
        this.row = row;
        this.col = col;
    }


    public int getRow() { return row; }
    public int getCol() { return col; }


    public boolean isEmpty() { return stone == null; }
    public Stone getStone() { return stone; }
    public void setStone(Stone s) { this.stone = s; }
}