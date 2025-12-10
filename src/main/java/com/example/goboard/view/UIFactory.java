package com.example.goboard.view;

/**
 * Factory for creating UI implementations.
 * This allows easy switching between different UI types without modifying game logic.
 * 
 * Currently supports:
 * - CONSOLE: Console-based text UI
 */
public class UIFactory {
    
    public enum UIType {
        CONSOLE
        // GUI will be added here in the future
    }
    
    /**
     * Create a GameUI instance based on the specified type.
     */
    public static GameUI createUI(UIType type) {
        switch (type) {
            case CONSOLE:
            default:
                return new ConsoleGameUI();
        }
    }
}
