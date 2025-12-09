package com.example.goboard.builder;


import com.example.goboard.model.Board;


public class BoardBuilder {
    private int size = 19; // default


    public BoardBuilder size(int s) { this.size = s; return this; }
    public Board build() { return new Board(size); }
}