package com.example.goboard.factory;


import com.example.goboard.model.Board;
import com.example.goboard.builder.BoardBuilder;


public class BoardFactory {
    public static Board standard19() { return new BoardBuilder().size(19).build(); }
    public static Board small9() { return new BoardBuilder().size(9).build(); }
    public static Board custom(int size) { return new BoardBuilder().size(size).build(); }
}