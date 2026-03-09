package fbs.lg1;

import java.util.Scanner;

public class ZahlenRaten {

    public int generateNumber() {
        return (int) (Math.random() * 100) + 1;
    }

    public int compare(int guess, int target) {
        if (guess < target)
            return -1;
        else if (guess > target)
            return 1;
        else
            return 0;
    }

    public String hint(int guess, int target) {
        int diff = Math.abs(guess - target);
        if (diff <= 5)
            return "warm";
        else
            return "kalt";
    }

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            int target = generateNumber();
            int attempts = 0;

            System.out.println("Errate meine Zahl zwischen 1 und 100");

            while (true) {
                System.out.print("Tipp: ");
                int guess = sc.nextInt();
                attempts++;

                int result = compare(guess, target);
                if (result == -1)
                    System.out.println("Zu klein! " + hint(guess, target));
                else if (result == 1)
                    System.out.println("Zu groß! " + hint(guess, target));
                else {
                    System.out.println("Richtig! Du hast " + attempts + " Versuche gebraucht.");
                    break;
                }
            }
        }
    }
}
