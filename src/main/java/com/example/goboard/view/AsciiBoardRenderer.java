package com.example.goboard.view;


import com.example.goboard.model.Board;
import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;


public class AsciiBoardRenderer implements BoardRenderer {
    private static final String COLUMNS = "ABCDEFGHJKLMNOPQRST"; // Skip 'I' in Go notation
    
    @Override
    public String render(Board board) {
        StringBuilder sb = new StringBuilder();
        int n = board.getSize();
        
        // Column headers
        sb.append("   ");
        for (int c = 0; c < n; c++) {
            sb.append(" ").append(COLUMNS.charAt(c));
        }
        sb.append('\n');
        
        // Board rows with row numbers
        for (int r = 0; r < n; r++) {
            // Row number (right-aligned)
            sb.append(String.format("%2d ", r + 1));
            
            for (int c = 0; c < n; c++) {
                Intersection it = board.getIntersection(r, c);
                if (it.isEmpty()) {
                    sb.append(" ·"); // Middle dot for empty intersection
                } else {
                    Stone stone = it.getStone();
                    if (stone.getColor() == Stone.Color.BLACK) {
                        sb.append(" ●"); // Black circle
                    } else {
                        sb.append(" ○"); // White circle
                    }
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}