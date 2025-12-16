package com.example.goboard.network.handler;

import com.example.goboard.network.ClientHandler;
import com.example.goboard.network.GameMessage;

/**
 * Handles MOVE messages - player stone placement.
 */
public class MoveHandler implements MessageHandler {
    
    @Override
    public void handle(MessageHandlerContext context, GameMessage message) {
        if (!context.isGameActive() || context.getOpponent() == null) {
            System.err.println("[MoveHandler] Game not active or no opponent");
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Game not active");
            context.sendMessage(response);
            return;
        }
        
        GameMessage.MoveMessage moveMsg = (GameMessage.MoveMessage) message;
        int row = moveMsg.getRow();
        int col = moveMsg.getCol();
        
        System.out.println("[MoveHandler] Processing move at (" + row + ", " + col + ") for player: " + context.getPlayerName());
        boolean success = context.getGameController().play(row, col);
        int[][] boardState = context.serializeBoard(context.getBoard());
        
        if (success) {
            System.out.println("[MoveHandler] Move accepted! Sending to opponent: " + context.getOpponent().getPlayerName());
            GameMessage response = new GameMessage.MoveResponseMessage(
                true,
                "Move accepted at (" + row + ", " + col + ")",
                boardState);
            context.sendMessage(response);
            
            // Notify opponent
            GameMessage opponentMsg = new GameMessage.OpponentMoveMessage(
                row, col,
                "Opponent played at (" + row + ", " + col + ")",
                boardState);
            context.getOpponent().sendMessage(opponentMsg);
        } else {
            System.out.println("[MoveHandler] Invalid move at (" + row + ", " + col + ")");
            GameMessage response = new GameMessage.MoveResponseMessage(
                false,
                "Invalid move at (" + row + ", " + col + ")",
                boardState);
            context.sendMessage(response);
        }
    }
}
