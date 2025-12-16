package com.example.goboard;

import java.util.Scanner;

import com.example.goboard.console.ConsoleGame;
import com.example.goboard.network.GameClient;
import com.example.goboard.network.GameServer;
import com.example.goboard.view.UIFactory;
import com.example.goboard.view.GameUI;
import com.example.goboard.view.ConsoleUIFormatter;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        ConsoleUIFormatter.printHeader("GO Game");
        System.out.println("1. Local Game (Console)");
        System.out.println("2. Start Game Server");
        System.out.println("3. Join Game as Client");
        System.out.print("\nChoose option (1-3): ");
        
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
                ConsoleUIFormatter.printError("Invalid choice");
                scanner.close();
        }
    }
    
    private static void playLocalGame(Scanner scanner) {
        ConsoleUIFormatter.printInfo("Starting local console game...");
        
        GameUI ui = UIFactory.createUI(UIFactory.UIType.CONSOLE);
        ConsoleGame game = new ConsoleGame(ui);
        game.start();
        
        scanner.close();
    }
    
    private static void startServer() {
        ConsoleUIFormatter.printInfo("Starting game server...");
        GameServer server = new GameServer();
        server.start();
    }
    
    private static void playAsClient(Scanner scanner) {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        
        GameClient client = new GameClient(name);
        
        if (!client.connect()) {
            ConsoleUIFormatter.printError("Failed to connect to server");
            scanner.close();
            return;
        }
        
        // Keep running until disconnected
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
