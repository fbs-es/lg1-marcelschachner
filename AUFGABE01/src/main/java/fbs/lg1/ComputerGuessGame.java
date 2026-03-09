package fbs.lg1;

import java.util.Scanner;

public class ComputerGuessGame {

    public void run(Scanner sc) {
        System.out.println("Denke dir eine Zahl aus. Der Computer wird sie mit deiner Hilfe erraten!");
        System.out.print("Suchmethode (1 = Binäre Suche, 2 = Zufällig): ");
        int mode = sc.nextInt();
        System.out.print("Untere Grenze: ");
        int uG = sc.nextInt();
        System.out.print("Obere Grenze: ");
        int oG = sc.nextInt();
        int attempts = 0;

        while (true) {
            int guess = (mode == 1) ? NumberGenerator.binarySearchGuess(uG, oG) : NumberGenerator.generateNumber(uG, oG);
            attempts++;
            System.out.println("Ist deine Zahl " + guess + "? (Kleiner/Größer/Gleich): ");
            String answer = sc.next();

            if (answer.equals("Gleich")) {
                System.out.println("Der Computer hat deine Zahl in " + attempts + " Versuchen erraten!");
                break;
            } else if (answer.equals("Kleiner")) {
                oG = guess - 1;
            } else if (answer.equals("Größer")) {
                uG = guess + 1;
            }
        }
    }
}
