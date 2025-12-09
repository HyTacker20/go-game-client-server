package com.example.goboard.observer;


import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;


public class ConsoleBoardListener implements BoardListener {
    @Override
    public void onStonePlaced(Board board, Intersection intersection) {
        System.out.printf("Kamien postawiony na (%d,%d)\n", intersection.getRow(), intersection.getCol());
    }
}