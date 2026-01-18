package com.example.goboard.network.handler;

import com.example.goboard.network.GameMessage;
import com.example.goboard.model.Stone;

/**
 * Handles PASS messages - player passing their turn.
 * When 2 consecutive passes occur, enters scoring phase.
 */
public class PassHandler implements MessageHandler {
    
    @Override
    public void handle(MessageHandlerContext context, GameMessage message) {
        if (!context.isGameActive() || context.getOpponent() == null) {
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Game not active");
            context.sendMessage(response);
            return;
        }

        if (context.getGameController().isScoringInProgress()) {
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Scoring in progress. Cannot pass until scoring is resolved.");
            context.sendMessage(response);
            return;
        }
        
        context.getGameController().pass();
        int consecutivePasses = context.getGameController().getConsecutivePasses();
        System.out.println("[GAME] ○ " + context.getPlayerName() + " passed their turn (passes: " + consecutivePasses + ")");
        
        int[][] boardState = context.serializeBoard(context.getBoard());
        int blackCaptured = context.getBoard().getBlackPrisoners();
        int whiteCaptured = context.getBoard().getWhitePrisoners();
        
        // Check if we've entered scoring phase (2 consecutive passes)
        if (consecutivePasses >= 2) {
            System.out.println("[GAME] Scoring phase initiated!");

            // Mark scoring lifecycle and reset confirmations
            context.getGameController().startScoringPhase();
            context.getGameController().resetScoreConfirmations();
            System.out.println("[GAME] Scoring started after two passes; confirmations reset.");

            // Clear any previous scoring removals
            context.getGameController().clearRemovedDuringScoring();

            // Notify both players to enter scoring phase (manual dead-stone marking)
            GameMessage phaseMsg = new GameMessage.BoardStateMessage(
                GameMessage.MessageType.SCORING_PHASE,
                boardState,
                "Scoring phase: mark dead stones",
                blackCaptured,
                whiteCaptured);
            context.sendMessage(phaseMsg);
            context.getOpponent().sendMessage(phaseMsg);

        } else {
            // Normal pass - game continues
            GameMessage response = new GameMessage.MoveResponseMessage(
                true,
                "You passed",
                boardState,
                blackCaptured,
                whiteCaptured);
            context.sendMessage(response);
            
            // Notify opponent
            GameMessage opponentMsg = new GameMessage.BoardStateMessage(
                GameMessage.MessageType.OPPONENT_PASS,
                boardState,
                "Opponent passed",
                blackCaptured,
                whiteCaptured);
            context.getOpponent().sendMessage(opponentMsg);
        }
    }
    
    /**
     * Perform server-side scoring logic without UI.
     * This includes:
     * 1. Detect seki regions
     * 2. Dead stones are not removed on server (clients handle dead stone marking)
     */
    private void performScoringOnServer(MessageHandlerContext context) {
        // Detect seki regions before scoring
        context.getBoard().detectSeki();
        System.out.println("[GAME] Seki detection completed");
    }
}
