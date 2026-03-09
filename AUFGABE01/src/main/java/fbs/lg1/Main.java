package fbs.lg1;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Willkommen beim Zahlen-Raten!");
            System.out.println("1: Du ratst die Zahl des Computers");
            System.out.println("2: Der Computer errät deine Zahl");
            System.out.print("Auswahl: ");
            int choice = sc.nextInt();

            ZahlenRaten game = new ZahlenRaten();
            if (choice == 1)
                game.runHumanGuesses();
            else if (choice == 2)
                game.runComputerGuesses();
        }
    }
}
