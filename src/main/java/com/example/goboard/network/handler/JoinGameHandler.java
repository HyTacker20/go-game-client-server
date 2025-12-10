package com.example.goboard.network.handler;

import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.network.GameMessage;

/**
 * Handles JOIN_GAME messages - player registration and initialization.
 */
public class JoinGameHandler implements MessageHandler {
    
    @Override
    public void handle(MessageHandlerContext context, GameMessage message) {
        GameMessage.JoinGameMessage joinMsg = (GameMessage.JoinGameMessage) message;
        String playerName = joinMsg.getPlayerName();
        String color = joinMsg.getPlayerColor();
        
        context.registerClient(playerName, context.getClientHandler());
        
        // Create player with specified color
        Stone.Color stoneColor = "WHITE".equalsIgnoreCase(color) ? 
            Stone.Color.WHITE : Stone.Color.BLACK;
        context.setPlayer(new Player(playerName, stoneColor));
        
        GameMessage response = new GameMessage.TextMessage(
            GameMessage.MessageType.WAITING, 
            "Waiting for opponent...");
        context.sendMessage(response);
        
        System.out.println("Player " + playerName + " (" + stoneColor + ") joined the game");
        
        // Mark as available for matching
        context.setAvailable(true);
    }
}
