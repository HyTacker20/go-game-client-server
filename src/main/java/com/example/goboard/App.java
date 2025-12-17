package com.example.goboard;

import java.util.Scanner;

import com.example.goboard.console.ConsoleGame;
import com.example.goboard.network.GameClient;
import com.example.goboard.network.GameServer;
import com.example.goboard.view.UIFactory;
import com.example.goboard.view.GameUI;
import com.example.goboard.view.ConsoleUIFormatter;

/**
 * Main application entry point.
 * Responsible for:
 * - displaying the main menu
 * - selecting game mode (local / server / client)
 * - initializing required components
 *
 * This class does NOT contain game logic.
 * It only coordinates startup flow.
 */
public class App {

    public static void main(String[] args) {
        // Scanner used for reading user input from console
        Scanner scanner = new Scanner(System.in);

        // Print application header
        ConsoleUIFormatter.printHeader("GO Game");

        // Main menu options
        System.out.println("1. Local Game (Console)");
        System.out.println("2. Start Game Server");
        System.out.println("3. Join Game as Client");
        System.out.print("\nChoose option (1-3): ");

        // Read user's menu choice
        String choice = scanner.nextLine().trim();

        // Select application mode based on user's choice
        switch (choice) {
            case "1":
                playLocalGame(scanner);
                break;
            case "2":
                startServer();
                break;
            case "3":
                playAsClient(scanner);
                break;
            default:
                ConsoleUIFormatter.printError("Invalid choice");
                scanner.close();
        }
    }

    /**
     * Starts a local console-based game.
     * Creates UI, initializes ConsoleGame and starts gameplay loop.
     */
    private static void playLocalGame(Scanner scanner) {
        ConsoleUIFormatter.printInfo("Starting local console game...");

        // Create console UI using factory
        GameUI ui = UIFactory.createUI(UIFactory.UIType.CONSOLE);

        // Create and start local game
        ConsoleGame game = new ConsoleGame(ui);
        game.start();

        // Close input stream after game ends
        scanner.close();
    }

    /**
     * Starts the game server.
     * The server waits for clients and manages network games.
     */
    private static void startServer() {
        ConsoleUIFormatter.printInfo("Starting game server...");
        GameServer server = new GameServer();
        server.start();
    }

    /**
     * Connects to an existing game server as a client.
     * Handles terminal mode switching and clean shutdown.
     */
    private static void playAsClient(Scanner scanner) {
        // Ask player for name
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();

        // Enable ANSI support (important for Windows terminals)
        // and switch to alternative screen buffer
        ConsoleUIFormatter.enableWindowsAnsiSupport();
        ConsoleUIFormatter.enterAlternativeScreen();

        // Create client instance
        GameClient client = new GameClient(name);

        // Try to connect to the server
        if (!client.connect()) {
            ConsoleUIFormatter.printError("Failed to connect to server");
            ConsoleUIFormatter.exitAlternativeScreen();
            scanner.close();
            return;
        }

        // Keep application running while client is connected
        try {
            while (client.isConnected()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Always restore original terminal screen
            ConsoleUIFormatter.exitAlternativeScreen();
            scanner.close();
        }
    }
}
