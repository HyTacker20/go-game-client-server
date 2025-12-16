package com.example.goboard.network.handler;

import com.example.goboard.network.GameMessage;

/**
 * Handles RESIGN messages - player resignation.
 */
public class ResignHandler implements MessageHandler {
    
    @Override
    public void handle(MessageHandlerContext context, GameMessage message) {
        if (context.isGameActive() && context.getOpponent() != null) {
            System.out.println("[GAME] ☠ " + context.getPlayerName() + " resigned. " + context.getOpponent().getPlayerName() + " wins!");
            
            GameMessage gameOverMsg = new GameMessage.TextMessage(
                GameMessage.MessageType.GAME_OVER,
                "Opponent resigned. You win!");
            context.getOpponent().sendMessage(gameOverMsg);
            
            GameMessage selfMsg = new GameMessage.TextMessage(
                GameMessage.MessageType.GAME_OVER,
                "You resigned. You lost.");
            context.sendMessage(selfMsg);
            
            endGame(context);
        }
    }
    
    private void endGame(MessageHandlerContext context) {
        context.setGameActive(false);
        if (context.getOpponent() != null) {
            context.getOpponent().setGameActive(false);
        }
        context.setAvailable(true);
        if (context.getOpponent() != null) {
            context.getOpponent().setAvailable(true);
        }
    }
}
