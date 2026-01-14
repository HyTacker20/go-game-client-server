package com.example.goboard.network.handler;

import com.example.goboard.network.GameMessage;

/**
 * Handles PASS messages - player passing their turn.
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
        
        context.getGameController().pass();
        System.out.println("[GAME] ○ " + context.getPlayerName() + " passed their turn");
        
        int[][] boardState = context.serializeBoard(context.getBoard());
        int blackCaptured = context.getBoard().getBlackPrisoners();
        int whiteCaptured = context.getBoard().getWhitePrisoners();
        
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
