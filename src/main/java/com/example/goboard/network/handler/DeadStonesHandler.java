package com.example.goboard.network.handler;

import com.example.goboard.model.Intersection;
import com.example.goboard.model.Stone;
import com.example.goboard.network.GameMessage;

/**
 * Handles DEAD_STONES messages: client submits marked dead stones.
 * Server removes them, stores for potential restore, detects seki, and sends SCORE_RESPONSE.
 */
public class DeadStonesHandler implements MessageHandler {
    @Override
    public void handle(MessageHandlerContext context, GameMessage message) {
        if (!(message instanceof GameMessage.DeadStonesMessage deadMsg)) {
            context.sendMessage(new GameMessage.TextMessage(
                    GameMessage.MessageType.ERROR,
                    "Invalid dead stones message"));
            return;
        }

        if (!context.isGameActive() || context.getOpponent() == null) {
            context.sendMessage(new GameMessage.TextMessage(
                    GameMessage.MessageType.ERROR,
                    "Game not active"));
            return;
        }

        if (!context.getGameController().isScoringInProgress()) {
            context.sendMessage(new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Not in scoring phase"));
            return;
        }

        int[][] positions = deadMsg.getPositions();
        if (positions == null) {
            positions = new int[0][0];
        }

        // Clear previous removals and apply new dead stones
        context.getGameController().clearRemovedDuringScoring();
        context.getGameController().resetScoreConfirmations();

        System.out.println("[GAME] Dead stones submitted: count=" + positions.length);

        for (int[] pos : positions) {
            if (pos == null || pos.length < 2) continue;
            int r = pos[0];
            int c = pos[1];
            Intersection it = context.getBoard().getIntersection(r, c);
            if (it != null && !it.isEmpty()) {
                Stone.Color color = it.getStone().getColor();
                context.getGameController().addRemovedDuringScoring(r, c, color);
                it.setStone(null); // remove - do NOT add to prisoners, just remove
            }
        }

        // Detect seki before scoring
        context.getBoard().detectSeki();

        double blackScore = context.getBoard().calculateScore(Stone.Color.BLACK);
        double whiteScore = context.getBoard().calculateScore(Stone.Color.WHITE);

        String blackName = context.getGameController().getBlackPlayer().getName();
        String whiteName = context.getGameController().getWhitePlayer().getName();

        int[][] boardState = context.serializeBoard(context.getBoard());

        GameMessage scoreMsg = new GameMessage.ScoreMessage(
                GameMessage.MessageType.SCORE_RESPONSE,
                blackScore,
                whiteScore,
                boardState,
                blackName,
                whiteName);

        // Send to both players
        context.sendMessage(scoreMsg);
        context.getOpponent().sendMessage(scoreMsg);
    }
}
