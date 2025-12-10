package com.example.goboard.observer;


import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;


public interface BoardListener {
    void onStonePlaced(Board board, Intersection intersection);
}