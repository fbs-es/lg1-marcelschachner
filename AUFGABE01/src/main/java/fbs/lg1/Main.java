package fbs.lg1;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Willkommen beim Zahlen-Raten!");
            System.out.println("1: Du rätst die Zahl des Computers");
            System.out.println("2: Der Computer errät deine Zahl");
            System.out.print("Auswahl: ");
            int choice = sc.nextInt();

            if (choice == 1)
                new HumanGuessGame().run(sc);
            else if (choice == 2)
                new ComputerGuessGame().run(sc);
        }
    }
}
