package com.example.goboard.network.handler;

import com.example.goboard.controller.GameController;
import com.example.goboard.factory.BoardFactory;
import com.example.goboard.model.Board;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.network.ClientHandler;
import com.example.goboard.network.GameMessage;
import com.example.goboard.strategy.SimpleMoveValidator;

/**
 * Handles START_GAME messages - initializes game between two players.
 */
public class StartGameHandler implements MessageHandler {
    
    @Override
    public void handle(MessageHandlerContext context, GameMessage message) {
        GameMessage.TextMessage textMsg = (GameMessage.TextMessage) message;
        String opponentName = textMsg.getMessage();
        ClientHandler opponent = context.findClient(opponentName);
        
        if (opponent == null) {
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Opponent not found: " + opponentName);
            context.sendMessage(response);
            return;
        }
        
        if (opponent.isGameActive()) {
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Opponent is already in a game");
            context.sendMessage(response);
            return;
        }
        
        // Initialize game
        Board board = BoardFactory.small9();
        context.setBoard(board);
        
        // Randomly assign colors to players, regardless of prior preference
        boolean contextIsBlack = Math.random() < 0.5;
        Player contextAssigned = new Player(context.getPlayer().getName(), contextIsBlack ? Stone.Color.BLACK : Stone.Color.WHITE);
        Player opponentAssigned = new Player(opponent.getPlayer().getName(), contextIsBlack ? Stone.Color.WHITE : Stone.Color.BLACK);

        // Update players on handlers with assigned colors
        context.setPlayer(contextAssigned);
        opponent.setPlayer(opponentAssigned);

        Player blackPlayer = contextIsBlack ? contextAssigned : opponentAssigned;
        Player whitePlayer = contextIsBlack ? opponentAssigned : contextAssigned;
        
        GameController gameController = new GameController(
            board, new SimpleMoveValidator(), 
            blackPlayer, whitePlayer, blackPlayer);
        
        context.setGameController(gameController);
        context.setGameActive(true);
        opponent.setGameActive(true);
        opponent.setOpponent(context.getClientHandler());
        opponent.setGameController(gameController);
        opponent.setBoard(board);
        
        context.setOpponent(opponent);
        context.setAvailable(false);
        opponent.setAvailable(false);
        
        // Notify both players
        int[][] boardState = context.serializeBoard(board);
        GameMessage startMsg = new GameMessage.BoardStateMessage(
            GameMessage.MessageType.YOUR_TURN,
            boardState,
            "Game started! Black plays first");
        
        if (contextAssigned.getColor() == Stone.Color.BLACK) {
            context.sendMessage(startMsg);
            
            GameMessage opponentMsg = new GameMessage.BoardStateMessage(
                GameMessage.MessageType.OPPONENT_TURN,
                boardState,
                "Game started! Opponent (Black) plays first");
            opponent.sendMessage(opponentMsg);
        } else {
            context.sendMessage(new GameMessage.BoardStateMessage(
                GameMessage.MessageType.OPPONENT_TURN,
                boardState,
                "Game started! Opponent (Black) plays first"));
            opponent.sendMessage(startMsg);
        }
        
        System.out.println("Game started: " + blackPlayer.getName() + " (BLACK) vs " + whitePlayer.getName() + " (WHITE)");
    }
}
