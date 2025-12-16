package com.example.goboard.view;

/**
 * Handles all console UI formatting for the GO game.
 * Provides consistent visual presentation with sections, separators, and styled messages.
 */
public class ConsoleUIFormatter {
    
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String BLUE = "\u001B[34m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    private static final int SECTION_WIDTH = 50;
    private static final String HORIZONTAL_LINE = "=".repeat(SECTION_WIDTH);
    private static final String SECTION_DIVIDER = "-".repeat(SECTION_WIDTH);
    
    /**
     * Prints a main header with title
     */
    public static void printHeader(String title) {
        System.out.println();
        System.out.println(BOLD + CYAN + "+" + HORIZONTAL_LINE + "+" + RESET);
        String centered = centerText(title, SECTION_WIDTH);
        System.out.println(BOLD + CYAN + "|" + RESET + centered + BOLD + CYAN + "|" + RESET);
        System.out.println(BOLD + CYAN + "+" + HORIZONTAL_LINE + "+" + RESET);
        System.out.println();
    }
    
    /**
     * Prints a section header with divider
     */
    public static void printSectionHeader(String section) {
        System.out.println(BOLD + YELLOW + section + RESET);
        System.out.println(SECTION_DIVIDER);
    }
    
    /**
     * Prints a success message with styling
     */
    public static void printSuccess(String message) {
        System.out.println(GREEN + "+ " + message + RESET);
    }
    
    /**
     * Prints an error message with styling
     */
    public static void printError(String message) {
        System.out.println(RED + "- " + message + RESET);
    }
    
    /**
     * Prints info message with styling
     */
    public static void printInfo(String message) {
        System.out.println(CYAN + "? " + message + RESET);
    }
    
    /**
     * Prints a warning message with styling
     */
    public static void printWarning(String message) {
        System.out.println(YELLOW + "? " + message + RESET);
    }
    
    /**
     * Prints a neutral message
     */
    public static void printMessage(String message) {
        System.out.println(message);
    }
    
    /**
     * Prints game status information
     */
    public static void printGameStatus(String playerName, String playerColor, String status) {
        printSectionHeader("Game Status");
        System.out.println("Player: " + BOLD + playerName + RESET);
        System.out.println("Color:  " + getColorDisplay(playerColor));
        System.out.println("Status: " + BOLD + status + RESET);
        System.out.println();
    }
    
    /**
     * Prints waiting status
     */
    public static void printWaiting(String message) {
        System.out.println(YELLOW + "? Waiting: " + message + RESET);
    }
    
    /**
     * Prints whose turn it is
     */
    public static void printTurnInfo(String playerName, String color, boolean isYourTurn) {
        System.out.println();
        if (isYourTurn) {
            System.out.println(BOLD + GREEN + "=============================" + RESET);
            System.out.println(BOLD + GREEN + "      >>> YOUR TURN <<<" + RESET);
            System.out.println(BOLD + GREEN + "=============================" + RESET);
        } else {
            System.out.println(BOLD + BLUE + "=============================" + RESET);
            System.out.println(BOLD + BLUE + "   Opponent's Turn" + RESET);
            System.out.println(BOLD + BLUE + "=============================" + RESET);
        }
        System.out.println();
    }
    
    /**
     * Prints opponent move information
     */
    public static void printOpponentMove(String opponentName, String position) {
        System.out.println();
        System.out.println(BOLD + BLUE + "Opponent Move:" + RESET);
        System.out.println("  " + opponentName + " played at " + BOLD + position + RESET);
        System.out.println();
    }
    
    /**
     * Prints move response
     */
    public static void printMoveResponse(boolean success, String message) {
        if (success) {
            printSuccess("Move accepted: " + message);
        } else {
            printError("Move rejected: " + message);
        }
    }
    
    /**
     * Prints move input prompt
     */
    public static void printMovePrompt() {
        System.out.print(BOLD + GREEN + "Your move (e.g., D4), 'pass', or 'resign': " + RESET);
    }
    
    /**
     * Prints game over screen
     */
    public static void printGameOver(String winner, String reason) {
        System.out.println();
        System.out.println(BOLD + RED + "+" + HORIZONTAL_LINE + "+" + RESET);
        System.out.println(BOLD + RED + "|" + centerText("GAME OVER", SECTION_WIDTH) + BOLD + RED + "|" + RESET);
        System.out.println(BOLD + RED + "+" + HORIZONTAL_LINE + "+" + RESET);
        System.out.println();
        System.out.println("Winner: " + BOLD + winner + RESET);
        System.out.println("Reason: " + reason);
        System.out.println();
    }
    
    /**
     * Prints the board with frame
     */
    public static void printBoardWithFrame(String boardString) {
        System.out.println();
        String[] lines = boardString.split("\n");
        
        // Find the maximum line length
        int maxLength = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty() && line.length() > maxLength) {
                maxLength = line.length();
            }
        }
        
        // Create frame
        String topLine = "+" + "-".repeat(Math.max(maxLength + 2, 20)) + "+";
        String bottomLine = topLine;
        
        System.out.println(CYAN + topLine + RESET);
        System.out.println(CYAN + "| " + BOLD + " ".repeat(Math.max(maxLength - 14, 1)) + "GO Board" + RESET + CYAN + " ".repeat(Math.max(maxLength - 14, 1)) + "|" + RESET);
        System.out.println(CYAN + topLine.replaceAll("[-]", "-") + RESET);
        
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                int padding = Math.max(0, maxLength - line.length());
                System.out.println(CYAN + "| " + RESET + line + " ".repeat(padding) + CYAN + " |" + RESET);
            }
        }
        System.out.println(CYAN + bottomLine + RESET);
        System.out.println();
    }
    
    /**
     * Prints connection status
     */
    public static void printConnecting(String host, int port) {
        printInfo("Connecting to server at " + host + ":" + port + "...");
    }
    
    /**
     * Prints connection established
     */
    public static void printConnected(String host, int port, String playerName) {
        printSuccess("Connected to server at " + host + ":" + port);
        System.out.println("  Your name: " + BOLD + playerName + RESET);
    }
    
    /**
     * Prints disconnection message
     */
    public static void printDisconnected(String reason) {
        printError("Disconnected: " + reason);
    }
    
    /**
     * Prints invalid input message
     */
    public static void printInvalidInput(String hint) {
        printError("Invalid input. " + hint);
    }
    
    /**
     * Prints opponent resigned
     */
    public static void printOpponentResigned(String opponentName) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "** " + opponentName + " has resigned! **" + RESET);
        System.out.println();
    }
    
    /**
     * Prints opponent passed
     */
    public static void printOpponentPassed(String opponentName) {
        System.out.println();
        System.out.println(BOLD + YELLOW + "-- " + opponentName + " passed their turn --" + RESET);
        System.out.println();
    }
    
    /**
     * Gets colored display of player color
     */
    private static String getColorDisplay(String color) {
        if ("BLACK".equalsIgnoreCase(color)) {
            return BOLD + "● Black" + RESET;
        } else if ("WHITE".equalsIgnoreCase(color)) {
            return BOLD + WHITE + "○ White" + RESET;
        } else {
            return "Random (will be assigned)";
        }
    }
    
    /**
     * Centers text within given width
     */
    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - padding;
        return " ".repeat(padding) + text + " ".repeat(rightPadding);
    }
    
    /**
     * Prints a blank line for spacing
     */
    public static void printBlankLine() {
        System.out.println();
    }
}
