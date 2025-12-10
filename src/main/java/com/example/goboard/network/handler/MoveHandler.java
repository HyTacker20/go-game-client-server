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
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Game not active");
            context.sendMessage(response);
            return;
        }
        
        GameMessage.MoveMessage moveMsg = (GameMessage.MoveMessage) message;
        int row = moveMsg.getRow();
        int col = moveMsg.getCol();
        
        boolean success = context.getGameController().play(row, col);
        int[][] boardState = context.serializeBoard(context.getBoard());
        
        if (success) {
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
            GameMessage response = new GameMessage.MoveResponseMessage(
                false,
                "Invalid move at (" + row + ", " + col + ")",
                boardState);
            context.sendMessage(response);
        }
    }
}
