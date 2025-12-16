package com.example.goboard.strategy;

import com.example.goboard.model.Board;
import com.example.goboard.model.Stone;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleMoveValidatorTest {
    @Test
    void validWhenInsideBoardAndEmpty() {
        Board b = new Board(5);
        SimpleMoveValidator v = new SimpleMoveValidator();
        assertTrue(v.isValid(b, 2, 3, new Stone(Stone.Color.BLACK)));
    }

    @Test
    void invalidWhenOutsideBoardOrOccupied() {
        Board b = new Board(5);
        SimpleMoveValidator v = new SimpleMoveValidator();
        assertFalse(v.isValid(b, -1, 0, new Stone(Stone.Color.BLACK)));
        assertFalse(v.isValid(b, 5, 5, new Stone(Stone.Color.BLACK)));
        b.placeStone(1,1, new Stone(Stone.Color.WHITE));
        assertFalse(v.isValid(b, 1,1, new Stone(Stone.Color.BLACK)));
    }
}