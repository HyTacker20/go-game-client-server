package com.example.goboard.network.handler;

import com.example.goboard.controller.state.GameOverState;
import com.example.goboard.controller.state.PlayingState;
import com.example.goboard.model.Player;
import com.example.goboard.model.Stone;
import com.example.goboard.network.GameMessage;

/**
 * Handles score confirmation/rejection from players during scoring phase.
 * Both players must confirm the score for the game to end.
 * If either rejects, the game returns to normal play.
 */
public class ScoreConfirmationHandler implements MessageHandler {

    @Override
    public void handle(MessageHandlerContext context, GameMessage message) {
        if (!context.isGameActive() || context.getOpponent() == null) {
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Game not active");
            context.sendMessage(response);
            return;
        }

        if (!context.getGameController().isScoringInProgress()) {
            System.out.println("[GAME] Ignoring score confirmation outside scoring phase from " + context.getPlayerName());
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.WAITING,
                "Score confirmation ignored because scoring is not active.");
            context.sendMessage(response);
            return;
        }
        
        if (!(message instanceof GameMessage.ScoreConfirmationMessage)) {
            GameMessage response = new GameMessage.TextMessage(
                GameMessage.MessageType.ERROR,
                "Invalid score confirmation message");
            context.sendMessage(response);
            return;
        }
        
        GameMessage.ScoreConfirmationMessage confirmMsg = (GameMessage.ScoreConfirmationMessage) message;
        boolean accepted = confirmMsg.isAccepted();
        
        String playerName = context.getPlayerName();
        System.out.println("[GAME] " + playerName + (accepted ? " ACCEPTED" : " REJECTED") + " the score");
        
        if (!accepted) {
            // Score rejected - resume game
            System.out.println("[GAME] Score rejected by " + playerName + ". Returning to normal play.");
            resumeGame(context);
            return;
        }
        
        // Player accepted - check if both players have confirmed
        boolean playerIsBlack = context.getPlayer().getColor() == Stone.Color.BLACK;

        synchronized (context.getGameController()) {
            context.getGameController().markScoreConfirmed(playerIsBlack ? Stone.Color.BLACK : Stone.Color.WHITE);

            if (context.getGameController().bothScoresConfirmed()) {
                System.out.println("[GAME] Both players accepted the score. Game over!");
                endGame(context);
                // DO NOT call endScoringPhase here - it will be called during game state transition
                return;
            }
        }
        
        // Still waiting for opponent
        String response_msg = "You confirmed the score. Waiting for opponent...";
        GameMessage response = new GameMessage.TextMessage(
            GameMessage.MessageType.WAITING,
            response_msg);
        context.sendMessage(response);
        
        // Notify opponent that we confirmed
        GameMessage opponentMsg = new GameMessage.TextMessage(
            GameMessage.MessageType.WAITING,
            playerName + " confirmed the score. Waiting for your confirmation...");
        context.getOpponent().sendMessage(opponentMsg);
    }
    
    /**
     * Resumes the game after score rejection.
     */
    private void resumeGame(MessageHandlerContext context) {
        // Restore removed stones
        context.getGameController().restoreRemovedDuringScoring();

        context.getGameController().endScoringPhase();
        context.getGameController().resetScoreConfirmations();

        // Swap turn so the rejecting player plays first (per rules)
        Player current = context.getGameController().getCurrentPlayer();
        Player next = current == context.getGameController().getBlackPlayer()
                ? context.getGameController().getWhitePlayer()
                : context.getGameController().getBlackPlayer();

        context.getGameController().setState(new PlayingState(
            context.getGameController(),
            context.getBoard(),
            context.getMoveValidator(),
            context.getGameController().getBlackPlayer(),
            context.getGameController().getWhitePlayer(),
            next,
            0  // Reset consecutive passes
        ));
        
        // Send updated board state to both
        int[][] boardState = context.serializeBoard(context.getBoard());
        int blackCaptured = context.getBoard().getBlackPrisoners();
        int whiteCaptured = context.getBoard().getWhitePrisoners();

        GameMessage yourTurnMsg = new GameMessage.BoardStateMessage(
            GameMessage.MessageType.YOUR_TURN,
            boardState,
            "Score rejected. Your turn.",
            blackCaptured,
            whiteCaptured);
        GameMessage oppTurnMsg = new GameMessage.BoardStateMessage(
            GameMessage.MessageType.OPPONENT_TURN,
            boardState,
            "Opponent rejected the score. Waiting...",
            blackCaptured,
            whiteCaptured);

        // The rejecting player is 'context' player; they move first
        context.sendMessage(yourTurnMsg);
        context.getOpponent().sendMessage(oppTurnMsg);
    }
    
    /**
     * Ends the game after both players accept the score.
     */
    private void endGame(MessageHandlerContext context) {
        context.getGameController().setState(new GameOverState(
            context.getGameController(),
            context.getBoard(),
            context.getMoveValidator(),
            context.getGameController().getBlackPlayer(),
            context.getGameController().getWhitePlayer(),
            context.getGameController().getCurrentPlayer(),
            2  // 2 consecutive passes led to scoring
        ));
        context.getGameController().clearRemovedDuringScoring();
        context.getGameController().endScoringPhase();
        
        // Calculate winner
        double blackScore = context.getBoard().calculateScore(Stone.Color.BLACK);
        double whiteScore = context.getBoard().calculateScore(Stone.Color.WHITE);
        
        // Debug logging
        int blackTerritory = context.getBoard().countTerritory(Stone.Color.BLACK);
        int whiteTerritory = context.getBoard().countTerritory(Stone.Color.WHITE);
        int blackPrisoners = context.getBoard().getBlackPrisoners();
        int whitePrisoners = context.getBoard().getWhitePrisoners();
        
        System.out.println("[SCORE DEBUG]");
        System.out.println("  Black Territory: " + blackTerritory + ", Prisoners: " + blackPrisoners + ", Score: " + blackScore);
        System.out.println("  White Territory: " + whiteTerritory + ", Prisoners: " + whitePrisoners + ", Score: " + whiteScore);
        System.out.println("  Total territory accounted: " + (blackTerritory + whiteTerritory));
        
        String result;
        if (blackScore > whiteScore) {
            result = "Black wins by " + (blackScore - whiteScore) + " points";
        } else if (whiteScore > blackScore) {
            result = "White wins by " + (whiteScore - blackScore) + " points";
        } else {
            result = "Game is a draw (tie)";
        }
        
        System.out.println("[GAME] " + result);
        System.out.println("[GAME] Black: " + blackScore + ", White: " + whiteScore);
        
        // Notify both players
        GameMessage gameOverMsg = new GameMessage.TextMessage(
            GameMessage.MessageType.GAME_OVER,
            result + ". Black: " + blackScore + ", White: " + whiteScore);
        
        context.sendMessage(gameOverMsg);
        context.getOpponent().sendMessage(gameOverMsg);
    }
}

