package com.example.goboard.model;


public class Player {
    private final String name;
    private final Stone.Color color;


    public Player(String name, Stone.Color color) {
        this.name = name; this.color = color;
    }


    public String getName() { return name; }
    public Stone.Color getColor() { return color; }
}