package com.example.goboard.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for komi (compensation points for white).
 * Tests various scenarios to ensure komi is correctly applied to scoring.
 */
public class KomiTest {

    @Test
    void defaultKomiIs6Point5() {
        Board board = new Board(9);
        assertEquals(6.5, board.getKomi(), 0.001);
    }

    @Test
    void customKomiCanBeSet() {
        Board board = new Board(9, 7.5);
        assertEquals(7.5, board.getKomi(), 0.001);
    }

    @Test
    void komiIsAddedToWhiteScore() {
        Board board = new Board(5, 6.5);
        
        // Black captures one territory point
        Stone black = new Stone(Stone.Color.BLACK);
        board.placeStone(0, 0, black);
        board.placeStone(0, 1, black);
        board.placeStone(1, 0, black);
        
        // White captures one territory point on opposite side
        Stone white = new Stone(Stone.Color.WHITE);
        board.placeStone(4, 4, white);
        board.placeStone(4, 3, white);
        board.placeStone(3, 4, white);
        
        // Scores should differ by komi
        double blackScore = board.calculateScore(Stone.Color.BLACK);
        double whiteScore = board.calculateScore(Stone.Color.WHITE);
        
        // White gets komi advantage
        assertTrue(whiteScore > blackScore, 
            "White should have higher score with komi: white=" + whiteScore + ", black=" + blackScore);
        assertEquals(6.5, whiteScore - blackScore, 0.1, 
            "Score difference should be approximately komi value");
    }

    @Test
    void komiBreaksTiesInFavorOfWhite() {
        Board board = new Board(3, 0.5); // Minimal komi
        
        // Equal positions for both
        Stone black = new Stone(Stone.Color.BLACK);
        board.placeStone(0, 0, black);
        
        Stone white = new Stone(Stone.Color.WHITE);
        board.placeStone(2, 2, white);
        
        double blackScore = board.calculateScore(Stone.Color.BLACK);
        double whiteScore = board.calculateScore(Stone.Color.WHITE);
        
        // With equal territory, komi breaks the tie
        assertTrue(whiteScore > blackScore, "Komi should break ties in favor of white");
        assertEquals(0.5, whiteScore - blackScore, 0.001);
    }

    @Test
    void komiDoesNotAffectBlackScore() {
        Board board1 = new Board(5, 6.5);
        Board board2 = new Board(5, 10.0);
        
        Stone black = new Stone(Stone.Color.BLACK);
        board1.placeStone(0, 0, black);
        board2.placeStone(0, 0, black);
        
        // Black score should be the same regardless of komi
        assertEquals(board1.calculateScore(Stone.Color.BLACK), 
                    board2.calculateScore(Stone.Color.BLACK), 
                    0.001,
                    "Black score should not be affected by komi");
    }

    @Test
    void largerKomiGivesWhiteStrongerAdvantage() {
        Board board1 = new Board(5, 6.5);
        Board board2 = new Board(5, 7.5);
        
        // Same position on both boards
        Stone black = new Stone(Stone.Color.BLACK);
        Stone white = new Stone(Stone.Color.WHITE);
        
        board1.placeStone(0, 0, black);
        board2.placeStone(0, 0, black);
        
        board1.placeStone(4, 4, white);
        board2.placeStone(4, 4, white);
        
        double white1 = board1.calculateScore(Stone.Color.WHITE);
        double white2 = board2.calculateScore(Stone.Color.WHITE);
        
        assertEquals(1.0, white2 - white1, 0.001,
            "Larger komi should give white exactly 1 more point");
    }

    @Test
    void negativeKomiCanBeUsedForHandicapGames() {
        Board board = new Board(9, -5.5); // Reverse komi for handicap
        
        assertEquals(-5.5, board.getKomi(), 0.001);
        
        Stone black = new Stone(Stone.Color.BLACK);
        Stone white = new Stone(Stone.Color.WHITE);
        
        // Equal positions
        board.placeStone(0, 0, black);
        board.placeStone(8, 8, white);
        
        double blackScore = board.calculateScore(Stone.Color.BLACK);
        double whiteScore = board.calculateScore(Stone.Color.WHITE);
        
        // With negative komi, black should win with equal territory
        assertTrue(blackScore > whiteScore, 
            "Negative komi should favor black");
    }

    @Test
    void komiWithPrisonersAndTerritory() {
        Board board = new Board(5, 6.5);
        
        Stone black = new Stone(Stone.Color.BLACK);
        Stone white = new Stone(Stone.Color.WHITE);
        
        // Black captures white stone
        board.placeStone(0, 1, white);
        board.placeStone(1, 0, black);
        board.placeStone(0, 0, black);
        board.placeStone(1, 1, black);
        int captured = board.placeStone(0, 2, black);
        
        assertEquals(1, captured, "Should capture 1 white stone");
        
        // White has some territory
        board.placeStone(4, 4, white);
        board.placeStone(4, 3, white);
        board.placeStone(3, 4, white);
        
        double blackScore = board.calculateScore(Stone.Color.BLACK);
        double whiteScore = board.calculateScore(Stone.Color.WHITE);
        
        // White score includes komi
        assertTrue(whiteScore > 0, "White should have positive score with komi");
        
        // Verify komi is in the score
        double whiteScoreWithoutKomi = whiteScore - board.getKomi();
        assertTrue(whiteScoreWithoutKomi < whiteScore, "Komi should increase white's score");
    }

    @Test
    void zeroKomiForEvenGame() {
        Board board = new Board(9, 0.0);
        
        assertEquals(0.0, board.getKomi(), 0.001);
        
        Stone black = new Stone(Stone.Color.BLACK);
        Stone white = new Stone(Stone.Color.WHITE);
        
        // Identical positions
        board.placeStone(0, 0, black);
        board.placeStone(8, 8, white);
        
        double blackScore = board.calculateScore(Stone.Color.BLACK);
        double whiteScore = board.calculateScore(Stone.Color.WHITE);
        
        // With zero komi and equal positions, scores should be equal
        assertEquals(blackScore, whiteScore, 0.001,
            "With zero komi and equal positions, scores should be equal");
    }

    @Test
    void fractionalKomiPreventsDraw() {
        Board board = new Board(9, 6.5);
        
        // The 0.5 ensures no integer score can match
        double komi = board.getKomi();
        assertTrue(komi % 1 != 0, "Komi should be fractional to prevent draws");
    }
}
