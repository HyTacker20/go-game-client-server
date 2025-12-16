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
        
        context.registerClient(playerName, context.getClientHandler());
        
        context.setPlayer(new Player(playerName, Stone.Color.UNASSIGNED));
        
        GameMessage response = new GameMessage.TextMessage(
            GameMessage.MessageType.WAITING, 
            "Waiting for opponent...");
        context.sendMessage(response);
        
        System.out.println("[LOBBY] ► " + playerName + " joined the lobby");
        
        // Mark as available for matching
        context.setAvailable(true);
    }
}
