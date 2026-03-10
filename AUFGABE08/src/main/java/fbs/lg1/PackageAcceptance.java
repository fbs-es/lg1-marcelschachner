package fbs.lg1;

import java.util.Scanner;

public class PackageAcceptance {

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enthält das Paket gefährliche Güter? (J/N): ");
            boolean dangerous = scanner.nextLine().trim().toUpperCase().equals("J");

            System.out.print("Gewicht: ");
            double weight = scanner.nextDouble();

            System.out.print("Länge: ");
            double length = scanner.nextDouble();

            System.out.print("Breite: ");
            double width = scanner.nextDouble();

            System.out.print("Höhe: ");
            double height = scanner.nextDouble();
            scanner.nextLine();

            PackageResult result = new PackageChecker().check(new Package(weight, length, width, height, false, dangerous));

            if (result != PackageResult.ACCEPTED) {
                System.out.println("Sperrgut-Verweis: Bitte geben Sie das Paket am Schalter ab.");
                return;
            }

            System.out.print("Inhalt zerbrechlich? (J/N): ");
            boolean fragile = scanner.nextLine().trim().toUpperCase().equals("J");

            System.out.println("Paket angenommen. Versandbestätigung wird gedruckt.");
            if (fragile)
                System.out.println("Vermerk: Vorsicht - zerbrechlicher Inhalt.");
        }
    }
}
