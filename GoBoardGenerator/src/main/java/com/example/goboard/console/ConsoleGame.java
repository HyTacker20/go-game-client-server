package com.example.goboard.console;

import java.util.Scanner;
import com.example.goboard.model.*;
import com.example.goboard.factory.BoardFactory;
import com.example.goboard.view.AsciiBoardRenderer;
import com.example.goboard.strategy.SimpleMoveValidator;
import com.example.goboard.controller.GameController;

public class ConsoleGame {

    private final Board board;
    private final GameController controller;
    private final AsciiBoardRenderer renderer = new AsciiBoardRenderer();
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleGame() {
        board = BoardFactory.standard19();
        controller = new GameController(
                board,
                new SimpleMoveValidator(),
                new Player("Black", Stone.Color.BLACK)
        );
    }

    public void start() {
        System.out.println("=== Gra GO — wersja konsolowa ===");
        System.out.println(renderer.render(board));

        while (true) {
            System.out.print("Ruch (np. D4), 'pass', 'quit': ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("quit")) {
                System.out.println("Koniec gry.");
                break;
            }

            if (input.equals("pass")) {
                System.out.println("Gracz pasuje.");
                controller.pass();
                continue;
            }

            int[] pos = parseMove(input);
            if (pos == null) {
                System.out.println("Niepoprawny format ruchu.");
                continue;
            }

            if (!controller.play(pos[0], pos[1])) {
                System.out.println("Nieprawidłowy ruch.");
            }

            System.out.println(renderer.render(board));
        }
    }

    /**
     * Parsuje notację typu A3, D10 itp.
     * W GO pomija się literę I.
     */
    private int[] parseMove(String move) {
        if (move.length() < 2) return null;

        char colChar = Character.toUpperCase(move.charAt(0));
        if (colChar < 'A' || colChar > 'T' || colChar == 'I') return null;

        int col = colChar - 'A';
        if (colChar > 'I') col--; // pominięcie I

        try {
            int row = Integer.parseInt(move.substring(1)) - 1; // w kodzie start od 0
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
