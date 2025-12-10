package com.example.goboard.model;


import java.util.ArrayList;
import java.util.List;
import com.example.goboard.observer.BoardListener;


public class Board {
    private final int size; // number of lines, e.g., 19
    private final Intersection[][] intersections;
    private final List<BoardListener> listeners = new ArrayList<>();


    public Board(int size) {
        if (size <= 0) throw new IllegalArgumentException("Size must be > 0");
        this.size = size;
        intersections = new Intersection[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                intersections[r][c] = new Intersection(r, c);
            }
        }
    }


    public int getSize() { return size; }


    public Intersection getIntersection(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) return null;
        return intersections[row][col];
    }


    public void addListener(BoardListener l) { listeners.add(l); }
    public void removeListener(BoardListener l) { listeners.remove(l); }


    public boolean placeStone(int row, int col, Stone stone) {
        Intersection it = getIntersection(row, col);
        if (it == null) return false;
        if (!it.isEmpty()) return false;
        it.setStone(stone);
        listeners.forEach(l -> l.onStonePlaced(this, it));
        return true;
    }
}