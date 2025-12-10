package com.example.goboard;

import java.util.Scanner;

import com.example.goboard.console.ConsoleGame;
import com.example.goboard.network.GameClient;
import com.example.goboard.network.GameServer;
import com.example.goboard.view.UIFactory;
import com.example.goboard.view.GameUI;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== GO Game ===");
        System.out.println("1. Local Game (Console)");
        System.out.println("2. Start Game Server");
        System.out.println("3. Join Game as Client");
        System.out.print("Choose option (1-3): ");
        
        String choice = scanner.nextLine().trim();
        
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
                System.out.println("Invalid choice");
                scanner.close();
        }
    }
    
    private static void playLocalGame(Scanner scanner) {
        System.out.println("\nStarting local console game...\n");
        
        GameUI ui = UIFactory.createUI(UIFactory.UIType.CONSOLE);
        ConsoleGame game = new ConsoleGame(ui);
        game.start();
        
        scanner.close();
    }
    
    private static void startServer() {
        System.out.println("\nStarting game server...");
        GameServer server = new GameServer();
        server.start();
    }
    
    private static void playAsClient(Scanner scanner) {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter your color (BLACK/WHITE) [default: BLACK]: ");
        String color = scanner.nextLine().trim().toUpperCase();
        if (!color.equals("BLACK") && !color.equals("WHITE")) {
            color = "BLACK";
        }
        
        GameClient client = new GameClient(name, color);
        
        if (!client.connect()) {
            System.out.println("Failed to connect to server");
            scanner.close();
            return;
        }
        
        System.out.println("\nConnected to server!");
        System.out.println("Your name: " + name);
        System.out.println("Your color: " + color);
        System.out.println("\nWaiting for opponent and game start...");
        
        // Keep running until disconnected
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
