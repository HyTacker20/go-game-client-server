package com.example.goboard.console;

import com.example.goboard.model.Board;
import com.example.goboard.view.GameUI;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleGameTest {

    /**
     * Fake implementation of GameUI for testing ConsoleGame.
     * Implements ALL methods of GameUI with minimal behavior.
     */
    static class FakeGameUI implements GameUI {

        private final Queue<String> inputs;

        final List<Board> displayedBoards = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        boolean closed = false;

        FakeGameUI(Queue<String> inputs) {
            this.inputs = inputs;
        }

        // =========================
        // GENERAL DISPLAY
        // =========================

        @Override
        public void displayBoard(Board board) {
            displayedBoards.add(board);
        }

        @Override
        public void displayScoringBoard(Board board) {
            displayedBoards.add(board);
        }

        @Override
        public void displayMessage(String message) {
            messages.add(message);
        }

        @Override
        public void displayScore(double blackScore, double whiteScore) {
            // no-op for tests
        }

        // =========================
        // GAMEPLAY INPUT
        // =========================

        @Override
        public String getMoveInput(String prompt) {
            return inputs.isEmpty() ? "quit" : inputs.poll();
        }

        @Override
        public String getStringInput(String prompt) {
            return getMoveInput(prompt);
        }

        @Override
        public int getIntegerInput(String prompt, int min, int max) {
            return min; // safe default
        }

        // =========================
        // SCORING PHASE
        // =========================

        @Override
        public int[] getDeadGroupSelection(String prompt) {
            return null; // finish marking immediately
        }

        @Override
        public boolean confirmScore(String playerName) {
            return true; // always accept score
        }

        // =========================
        // CLEANUP
        // =========================

        @Override
        public void close() {
            closed = true;
        }
    }

    @Test
    void quitImmediately_shouldExitGame() {
        Queue<String> inputs = new ArrayDeque<>();
        inputs.add("quit");

        FakeGameUI ui = new FakeGameUI(inputs);
        ConsoleGame game = new ConsoleGame(ui);

        game.start();

        assertTrue(ui.closed);
        assertFalse(ui.displayedBoards.isEmpty());
    }

    @Test
    void invalidMove_shouldNotCrashGame() {
        Queue<String> inputs = new ArrayDeque<>();
        inputs.add("ZZZ");
        inputs.add("quit");

        FakeGameUI ui = new FakeGameUI(inputs);
        ConsoleGame game = new ConsoleGame(ui);

        assertDoesNotThrow(game::start);
        assertTrue(ui.closed);
    }

    @Test
    void twoPasses_shouldReachScoringPhase() {
        Queue<String> inputs = new ArrayDeque<>();
        inputs.add("pass");
        inputs.add("pass");
        inputs.add("quit");

        FakeGameUI ui = new FakeGameUI(inputs);
        ConsoleGame game = new ConsoleGame(ui);

        game.start();

        assertTrue(ui.displayedBoards.size() >= 3);
        assertTrue(ui.closed);
    }

    @Test
    void resign_shouldEndGameImmediately() {
        Queue<String> inputs = new ArrayDeque<>();
        inputs.add("resign");
        inputs.add("D4"); // should never be read

        FakeGameUI ui = new FakeGameUI(inputs);
        ConsoleGame game = new ConsoleGame(ui);

        game.start();

        assertTrue(ui.closed);
        assertEquals(1, ui.displayedBoards.size());
    }
}
