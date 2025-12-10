package com.example.goboard.network.handler;

import com.example.goboard.network.GameMessage;

/**
 * Strategy/Command pattern interface for handling different message types.
 * Each message type gets its own handler implementation, reducing complexity
 * in the ClientHandler class.
 */
public interface MessageHandler {
    /**
     * Handle the received message.
     * 
     * @param context The handler context containing client state and utilities
     * @param message The message to handle
     */
    void handle(MessageHandlerContext context, GameMessage message);
}
