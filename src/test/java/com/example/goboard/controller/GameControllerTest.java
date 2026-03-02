package com.example.goboard.controller;

import com.example.goboard.controller.state.PlayingState;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.strategy.SimpleMoveValidator;
import com.example.goboard.view.GameUI;
import com.example.goboard.factory.BoardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    /**
     * Minimal fake UI for scoring phase.
     * Only required so handleScoring() can be called safely.
     */
    static class DummyUI implements GameUI {

        @Override public void displayBoard(Board board) {}
        @Override public void displayScoringBoard(Board board) {}
        @Override public void displayMessage(String message) {}
        @Override public void displayScore(double blackScore, double whiteScore) {}
        @Override public String getMoveInput(String prompt) { return "quit"; }
        @Override public String getStringInput(String prompt) { return ""; }
        @Override public int getIntegerInput(String prompt, int min, int max) { return min; }
        @Override public int[] getDeadGroupSelection(String prompt) { return null; }
        @Override public boolean confirmScore(String playerName) { return true; }
        @Override public void close() {}
    }

    private GameController newController() {
        Board board = BoardFactory.small9();
        return new GameController(
                board,
                new SimpleMoveValidator(),
                Player.defaultBlack(),
                Player.defaultWhite()
        );
    }

    @Test
    void initialState_blackStarts_gameNotOver() {
        GameController controller = newController();

        assertFalse(controller.isGameOver());
        assertFalse(controller.isScoringPhase());
        assertEquals(Stone.Color.BLACK,
                controller.getCurrentPlayer().getColor());

        assertTrue(controller.getState() instanceof PlayingState);
    }

    @Test
    void validMove_shouldSwitchTurn() {
        GameController controller = newController();

        Player first = controller.getCurrentPlayer();
        boolean ok = controller.play(0, 0);

        assertTrue(ok);
        assertNotEquals(first, controller.getCurrentPlayer(),
                "Turn should switch after a valid move");
    }

    @Test
    void pass_shouldIncreaseConsecutivePasses() {
        GameController controller = newController();

        controller.pass();

        assertEquals(1, controller.getConsecutivePasses());
        assertFalse(controller.isScoringPhase());
    }

    @Test
    void twoConsecutivePasses_shouldEnterScoringPhase() {
        GameController controller = newController();

        controller.pass();
        controller.pass();

        assertEquals(2, controller.getConsecutivePasses());
        assertTrue(controller.isScoringPhase(),
                "Two passes should trigger scoring phase");
        assertFalse(controller.isGameOver());
    }

    @Test
    void handleScoring_shouldNotCrash() {
        GameController controller = newController();

        controller.pass();
        controller.pass();

        assertTrue(controller.isScoringPhase());

        DummyUI ui = new DummyUI();

        assertDoesNotThrow(() -> controller.handleScoring(ui));
    }
}
