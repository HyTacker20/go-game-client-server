package com.example.goboard.view;


import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;


public class AsciiBoardRenderer implements BoardRenderer {
    @Override
    public String render(Board board) {
        StringBuilder sb = new StringBuilder();
        int n = board.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                Intersection it = board.getIntersection(r, c);
                if (it.isEmpty()) sb.append("+"); else sb.append(it.getStone().toString());
                if (c < n-1) sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}