package com.example.goboard.strategy;


import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;


public class SimpleMoveValidator implements MoveValidator {
    @Override
    public boolean isValid(Board board, int row, int col, Stone stone) {
        Intersection it = board.getIntersection(row, col);
        if (it == null) return false;
        return it.isEmpty();
    }
}