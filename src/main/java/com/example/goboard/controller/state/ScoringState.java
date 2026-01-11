package com.example.goboard.controller.state;

import com.example.goboard.controller.GameController;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.model.Intersection;
import com.example.goboard.strategy.MoveValidator;
import com.example.goboard.view.GameUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the SCORING PHASE of a Go game (Japanese rules).
 *
 * Entered after two consecutive passes.
 *
 * Responsibilities:
 * 1. Allow both players to mark dead stones
 * 2. Remove dead stones and count prisoners
 * 3. Calculate final score (territory + prisoners)
 * 4. Ask both players to confirm the result
 * 5. Resume the game if the score is rejected
 * 6. End the game if both players accept
 */
public class ScoringState extends AbstractGameState {

    // Stones removed during scoring (for restore if needed)
    private final List<Intersection> removedDuringScoring = new ArrayList<>();

    public ScoringState(GameController controller,
                        Board board,
                        MoveValidator validator,
                        Player blackPlayer,
                        Player whitePlayer,
                        Player currentPlayer,
                        int consecutivePasses) {

        super(controller, board, validator,
                blackPlayer, whitePlayer,
                currentPlayer, consecutivePasses);
    }

    /**
     * Any move during scoring means the score is rejected
     * and the game returns to normal play.
     *
     * The opponent moves first after resuming.
     */
    @Override
    public boolean play(int row, int col) {
        restoreDeadStones();
        swapPlayer(); // opponent moves first
        resumeGame();
        return true;
    }

    /**
     * Passing after score acceptance ends the game.
     */
    @Override
    public boolean pass() {
        controller.setState(new GameOverState(
                controller,
                board,
                validator,
                blackPlayer,
                whitePlayer,
                currentPlayer,
                consecutivePasses
        ));
        return true;
    }

    /**
     * Scoring itself is NOT game over.
     */
    @Override
    public boolean isGameOver() {
        return false;
    }

    /**
     * Main scoring workflow:
     *
     * 1. Both players mark dead stones
     * 2. Dead stones are removed from the board
     * 3. Final score is calculated
     * 4. Both players confirm the result
     * 5. Resume or finish the game
     */
    public void handleScoring(GameUI ui) {

        /* =====================================================
           DEAD STONE MARKING PHASE
           Each player marks dead stones once.
           ===================================================== */
        for (int i = 0; i < 2; i++) {

            ui.displayScoringBoard(board);
            ui.displayMessage(
                    "Mark dead stones for " + currentPlayer.getName()
                            + ". Press ENTER when done."
            );

            int[] pos;
            while ((pos = ui.getDeadGroupSelection(
                    currentPlayer.getName() + ", mark a dead stone: "
            )) != null) {

                Intersection it = board.getIntersection(pos[0], pos[1]);
                if (it != null && !it.isEmpty()) {
                    // Toggle dead marker (mark / unmark)
                    it.setMarkedDead(!it.isMarkedDead());
                }

                ui.displayScoringBoard(board);
            }

            swapPlayer();
        }

        /* =====================================================
           REMOVE DEAD STONES (Japanese rules)
           Dead stones are removed BEFORE territory counting
           ===================================================== */
        removeDeadStones();

        /* =====================================================
           SCORE CALCULATION
           territory + prisoners
           ===================================================== */
        int blackScore =
                board.countTerritory(Stone.Color.BLACK)
                        - board.countStones(Stone.Color.WHITE);

        int whiteScore =
                board.countTerritory(Stone.Color.WHITE)
                        - board.countStones(Stone.Color.BLACK);

        ui.displayScore(blackScore, whiteScore);

        /* =====================================================
           SCORE CONFIRMATION
           BOTH players must accept
           ===================================================== */
        if (!ui.confirmScore(blackPlayer.getName())) {
            restoreDeadStones();
            resumeGame();
            return;
        }

        if (!ui.confirmScore(whitePlayer.getName())) {
            restoreDeadStones();
            resumeGame();
            return;
        }

        /* =====================================================
           BOTH ACCEPTED → GAME OVER
           ===================================================== */
        pass();
    }

    /**
     * Removes all stones marked as dead.
     * They are counted as prisoners by Board.
     */
    private void removeDeadStones() {
        removedDuringScoring.clear();

        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {

                Intersection it = board.getIntersection(r, c);
                if (it != null && !it.isEmpty() && it.isMarkedDead()) {
                    removedDuringScoring.add(it);
                    it.setStone(null);
                }
            }
        }
    }

    /**
     * Restores stones removed during scoring
     * if the score is rejected.
     */
    private void restoreDeadStones() {
        for (Intersection it : removedDuringScoring) {
            it.setStone(new Stone(it.getMarkedDeadColor()));
            it.setMarkedDead(false);
        }
        removedDuringScoring.clear();
    }

    /**
     * Returns the game to normal play after score rejection.
     * Consecutive passes are reset.
     */
    private void resumeGame() {
        controller.setState(new PlayingState(
                controller,
                board,
                validator,
                blackPlayer,
                whitePlayer,
                currentPlayer,
                0
        ));
    }
}
