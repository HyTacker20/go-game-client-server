package com.example.goboard.strategy;


import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;


public interface MoveValidator {
    boolean isValid(Board board, int row, int col, Stone stone);
}