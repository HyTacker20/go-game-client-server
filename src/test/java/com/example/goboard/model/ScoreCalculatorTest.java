package com.example.goboard.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreCalculatorTest {

    private Board board;
    private ScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        board = new Board(5, 6.5); // small 5x5 board with komi
        calculator = new ScoreCalculator(board, 6.5);

        // Fill the entire board with WHITE stones as baseline
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                board.getIntersection(r, c).setStone(new Stone(Stone.Color.WHITE));
            }
        }
    }

    /* ======================================================
       SINGLE COLOR TERRITORY
       ====================================================== */
    @Test
    void testCountTerritorySingleColor() {
        // Empty intersection at (2,2)
        board.getIntersection(2,2).setStone(null);

        // Cage it with BLACK stones so flood-fill only counts 1
        int[][] blackPositions = {
                {1,2},{2,1},{2,3},{3,2}
        };
        for (int[] pos : blackPositions) {
            board.getIntersection(pos[0], pos[1]).setStone(new Stone(Stone.Color.BLACK));
        }

        int blackTerritory = calculator.countTerritory(Stone.Color.BLACK);
        int whiteTerritory = calculator.countTerritory(Stone.Color.WHITE);

        assertEquals(1, blackTerritory, "Black controls exactly the single enclosed intersection");
        assertEquals(0, whiteTerritory, "White controls no territory because all other intersections are stones");
    }

    /* ======================================================
       NEUTRAL TERRITORY
       ====================================================== */
    @Test
    void testCountTerritoryNeutralRegion() {
        // Empty intersection at (2,2)
        board.getIntersection(2,2).setStone(null);

        // Surround with BLACK and WHITE stones
        board.getIntersection(1,2).setStone(new Stone(Stone.Color.BLACK));
        board.getIntersection(2,1).setStone(new Stone(Stone.Color.WHITE));
        board.getIntersection(2,3).setStone(new Stone(Stone.Color.BLACK));
        board.getIntersection(3,2).setStone(new Stone(Stone.Color.WHITE));

        int blackTerritory = calculator.countTerritory(Stone.Color.BLACK);
        int whiteTerritory = calculator.countTerritory(Stone.Color.WHITE);

        assertEquals(0, blackTerritory, "Black should have no neutral territory");
        assertEquals(0, whiteTerritory, "White should have no neutral territory");
    }

    /* ======================================================
       SEKI TEST
       ====================================================== */
    @Test
    void testSekiExcludedFromTerritory() {
        Intersection seki = board.getIntersection(2,2);
        seki.setInSeki(true);

        int blackTerritory = calculator.countTerritory(Stone.Color.BLACK);
        int whiteTerritory = calculator.countTerritory(Stone.Color.WHITE);

        assertEquals(0, blackTerritory, "Seki intersections are ignored for black");
        assertEquals(0, whiteTerritory, "Seki intersections are ignored for white");
    }

    /* ======================================================
       SCORE TEST WITH KOMI
       ====================================================== */
    @Test
    void testCalculateScoreWithKomi() {
        // Empty intersection at (2,2) for BLACK territory
        board.getIntersection(2,2).setStone(null);

        // Cage it with BLACK stones
        int[][] blackPositions = {
                {1,2},{2,1},{2,3},{3,2}
        };
        for (int[] pos : blackPositions) {
            board.getIntersection(pos[0], pos[1]).setStone(new Stone(Stone.Color.BLACK));
        }

        double blackScore = calculator.calculateScore(
                Stone.Color.BLACK,
                board.getBlackPrisoners(),
                board.getWhitePrisoners()
        );

        double whiteScore = calculator.calculateScore(
                Stone.Color.WHITE,
                board.getBlackPrisoners(),
                board.getWhitePrisoners()
        );

        assertEquals(1, blackScore, "Black score = 1 territory, no prisoners");
        assertEquals(6.5, whiteScore, "White score = komi 6.5, no territory or prisoners");
    }

    /* ======================================================
       PRISONER TEST
       ====================================================== */
    @Test
    void testCountPrisonersIncludesMarkedDead() {
        // Black stone
        board.getIntersection(0,0).setStone(new Stone(Stone.Color.BLACK));
        // White stone marked dead
        Intersection deadWhite = board.getIntersection(0,1);
        deadWhite.setStone(new Stone(Stone.Color.WHITE));
        deadWhite.setMarkedDead(true);

        int blackPrisoners = calculator.countPrisoners(
                Stone.Color.BLACK,
                board.getBlackPrisoners(),
                board.getWhitePrisoners()
        );
        int whitePrisoners = calculator.countPrisoners(
                Stone.Color.WHITE,
                board.getBlackPrisoners(),
                board.getWhitePrisoners()
        );

        assertEquals(1, blackPrisoners, "Black counts 1 dead white stone");
        assertEquals(0, whitePrisoners, "White has no prisoners");
    }
}
