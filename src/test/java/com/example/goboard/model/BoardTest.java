package com.example.goboard.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    @Test
    void createsBoardWithSizeAndEmptyIntersections() {
        Board b = new Board(9);
        assertEquals(9, b.getSize());
        assertNotNull(b.getIntersection(0,0));
        assertTrue(b.getIntersection(0,0).isEmpty());
        assertNull(b.getIntersection(-1,0));
        assertNull(b.getIntersection(9,9));
    }

    @Test
    void placeStoneSucceedsOnEmptyAndCountsLiberties() {
        Board b = new Board(5);
        Stone black = new Stone(Stone.Color.BLACK);
        int captured = b.placeStone(2, 2, black);
        assertEquals(0, captured);
        assertFalse(b.getIntersection(2,2).isEmpty());
        assertEquals(black, b.getIntersection(2,2).getStone());
        assertEquals(4, b.countSingleStoneLiberties(2,2));
    }

    @Test
    void adjacentEnemyWithNoLibertyIsCaptured() {
        Board b = new Board(3);
        Stone black = new Stone(Stone.Color.BLACK);
        Stone white = new Stone(Stone.Color.WHITE);
        // place white at center (1,1)
        assertEquals(0, b.placeStone(1,1, white));
        // surround white on three sides, leave (1,2) empty
        assertEquals(0, b.placeStone(0,1, black));
        assertEquals(0, b.placeStone(1,0, black));
        assertEquals(0, b.placeStone(2,1, black));
        // now placing black at (1,2) should capture the white
        int captured = b.placeStone(1,2, black);
        assertEquals(1, captured);
        assertTrue(b.getIntersection(1,1).isEmpty());
    }

    @Test
    void suicideIsNotAllowedAndMoveIsReverted() {
        Board b = new Board(3);
        Stone black = new Stone(Stone.Color.BLACK);
        Stone white = new Stone(Stone.Color.WHITE);
        // Create a spot with no liberties if black plays at (1,1)
        assertEquals(0, b.placeStone(0,1, white));
        assertEquals(0, b.placeStone(1,0, white));
        assertEquals(0, b.placeStone(2,1, white));
        assertEquals(0, b.placeStone(1,2, white));
        int result = b.placeStone(1,1, black);
        assertEquals(-1, result);
        assertTrue(b.getIntersection(1,1).isEmpty());
    }
}