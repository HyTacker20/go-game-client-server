package com.example.goboard.model;

import java.util.ArrayList;
import java.util.List;
import com.example.goboard.observer.BoardListener;

public class Board {
    private final int size;
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

    /**
     * Publiczna metoda używana przez GameController.
     * Teraz po prostu korzysta z placeSimple().
     */
    public int placeStone(int row, int col, Stone stone) {
        return placeSimple(row, col, stone);
    }

    /**
     * Najprostsza implementacja zasad Go:
     * - pojedyncze kamienie
     * - liczenie oddechów
     * - usuwanie martwych sąsiadów
     * - zakaz samobójstwa
     */
    public int placeSimple(int row, int col, Stone stone) {
        Intersection it = getIntersection(row, col);
        if (it == null || !it.isEmpty()) return -1;

        it.setStone(stone);

        int captured = 0;

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        // spróbuj zbić jednego sąsiada
        for (int[] d : dirs) {
            Intersection n = getIntersection(row + d[0], col + d[1]);
            if (n == null || n.isEmpty()) continue;

            if (n.getStone().getColor() != stone.getColor()) {
                if (removeSingleStoneIfDead(n.getRow(), n.getCol())) {
                    captured++;
                }
            }
        }

        // sprawdź samobójstwo
        if (countSingleStoneLiberties(row, col) == 0) {
            it.setStone(null);
            return -1;
        }

        return captured;
    }

    public int countSingleStoneLiberties(int row, int col) {
        Intersection it = getIntersection(row, col);
        if (it == null || it.isEmpty()) return 0;

        int liberties = 0;

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        for (int[] d : dirs) {
            Intersection n = getIntersection(row + d[0], col + d[1]);
            if (n != null && n.isEmpty()) {
                liberties++;
            }
        }

        return liberties;
    }

    public boolean removeSingleStoneIfDead(int row, int col) {
        if (countSingleStoneLiberties(row, col) == 0) {
            Intersection it = getIntersection(row, col);
            if (it != null && !it.isEmpty()) {
                it.setStone(null);
                return true;
            }
        }
        return false;
    }
}
