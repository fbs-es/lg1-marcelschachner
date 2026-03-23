package fbs.lg1.checks;

import java.util.Scanner;

import fbs.lg1.Package;
import fbs.lg1.kosten.DeliveryCost;
import fbs.lg1.kosten.ShippingDestination;
import fbs.lg1.kosten.ShippingType;
import fbs.lg1.kosten.Size;

public class PackageAcceptance {

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enthält das Paket gefährliche Güter? (J/N): ");
            boolean dangerous = scanner
                    .nextLine()
                    .trim()
                    .toUpperCase()
                    .equals("J");

            System.out.print("Gewicht: ");
            double weight = scanner.nextDouble();

            System.out.print("Länge: ");
            double length = scanner.nextDouble();

            System.out.print("Breite: ");
            double width = scanner.nextDouble();

            System.out.print("Höhe: ");
            double height = scanner.nextDouble();
            scanner.nextLine();

            PackageResult result = new PackageChecker().check(
                    new Package(weight, length, width, height, false, dangerous));

            if (result != PackageResult.ACCEPTED) {
                System.out.println(
                        "Sperrgut-Verweis: Bitte geben Sie das Paket am Schalter ab.");
                return;
            }

            System.out.print("Inhalt zerbrechlich? (J/N): ");
            boolean fragile = scanner
                    .nextLine()
                    .trim()
                    .toUpperCase()
                    .equals("J");

            System.out.print("Größe (S/M/L): ");
            Size size = Size.valueOf(scanner.nextLine().trim().toUpperCase());

            System.out.print("Versand ins Ausland? (J/N): ");
            ShippingDestination destination = scanner
                    .nextLine()
                    .trim()
                    .toUpperCase()
                    .equals("J") ? ShippingDestination.AUSLAND : ShippingDestination.INLAND;

            System.out.print("Expressversand? (J/N): ");
            ShippingType type = scanner
                    .nextLine()
                    .trim()
                    .toUpperCase()
                    .equals("J") ? ShippingType.EXPRESS : ShippingType.NORMAL;

            DeliveryCost cost = new DeliveryCost();
            double total = cost.calculateBasePrice(size) + cost.calculateAdditionalCost(destination, type);

            System.out.println("Paket angenommen. Versandbestätigung wird gedruckt.");
            if (fragile)
                System.out.println("Vermerk: Vorsicht - zerbrechlicher Inhalt.");
            System.out.printf("Versandkosten: %.2f €%n", total);
        }
    }
}
