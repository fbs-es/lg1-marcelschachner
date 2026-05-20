package fbs.lg1;

import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

import fbs.lg1.cointray.Muenzen;
import fbs.lg1.inventory.Drinks;
import fbs.lg1.inventory.Inventory;
import fbs.lg1.util.CurrencyFormatter;

public class Getraenkautomat {

    private final Inventory inventory;

    public Getraenkautomat() {
        inventory = new Inventory();
        for (Drinks drink : Drinks.values()) {
            inventory.setStockQuantity(drink, 5);
        }
        for (Muenzen muenze : Muenzen.values()) {
            inventory.setCoinCount(muenze, 10);
        }
    }

    public EnumMap<Drinks, Integer> pruefeNachfuellbedarf() {
        return inventory.pruefeNachfuellbedarf();
    }

    public EnumMap<Drinks, Integer> ermittleBedarf() {
        return inventory.ermittleBedarf();
    }

    public void auffuellen(Map<Drinks, Integer> lieferung) {
        inventory.auffuellen(lieferung);
    }

    public int berechneKassenstand() {
        return inventory.berechneKassenstand();
    }

    public static int berechneEinwurf(Map<Muenzen, Integer> einwurf) {
        int summe = 0;
        for (Map.Entry<Muenzen, Integer> entry : einwurf.entrySet()) {
            summe += entry.getKey().getValueInCents() * entry.getValue();
        }
        return summe;
    }

    public boolean kannWechseln(int betrag, Map<Muenzen, Integer> einwurf) {
        return inventory.kannWechseln(betrag, einwurf);
    }

    public boolean verkaufe(Drinks drink, Map<Muenzen, Integer> einwurf) {
        if (inventory.getStockQuantity(drink) == 0) {
            System.out.println(drink.getName() + " ist ausverkauft.");
            return false;
        }
        int gezahlt = berechneEinwurf(einwurf);
        int preis = drink.getPriceInCents();
        if (gezahlt < preis) {
            System.out.println("Zu wenig. Noch " + CurrencyFormatter.toEuroFormat(preis - gezahlt) + " fehlen.");
            return false;
        }
        int wechsel = gezahlt - preis;
        if (wechsel > 0 && !kannWechseln(wechsel, einwurf)) {
            System.out.println("Kein Wechselgeld möglich. Bitte passend zahlen.");
            return false;
        }
        if (wechsel > 0) {
            Map<Muenzen, Integer> change = inventory.gebeWechselgeld(wechsel, einwurf);
            if (change == null) {
                System.out.println("Kein Wechselgeld möglich. Bitte passend zahlen.");
                return false;
            }
            System.out.println("Wechselgeld " + CurrencyFormatter.toEuroFormat(wechsel) + ":");
            change.forEach((m, n) -> System.out.println("  " + n + "x " + m.getDescription()));
        } else {
            einwurf.forEach((m, n) -> inventory.setCoinCount(m, inventory.getCoinCount(m) + n));
        }
        ausgabe(drink);
        return true;
    }

    private void ausgabe(Drinks drink) {
        inventory.dispense(drink);
        System.out.println(drink.getName() + " ausgegeben.");
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Getränkeautomat");

        while (true) {
            System.out.println();
            Drinks[] drinks = Drinks.values();
            for (int i = 0; i < drinks.length; i++) {
                String zeile = (i + 1) + ". " + drinks[i].getName() + " - "
                        + CurrencyFormatter.toEuroFormat(drinks[i].getPriceInCents());
                if (inventory.getStockQuantity(drinks[i]) == 0)
                    zeile += " [ausverkauft]";
                System.out.println(zeile);
            }
            System.out.println("0. Beenden");
            System.out.print("Wahl: ");

            int wahl = readInt(scanner);
            if (wahl == 0)
                break;
            if (wahl < 1 || wahl > drinks.length) {
                System.out.println("Ungültige Eingabe.");
                continue;
            }

            Drinks drink = drinks[wahl - 1];
            boolean abgeschlossen = false;

            for (int i = 1; i <= 3 && !abgeschlossen; i++) {
                Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
                int gezahlt = 0;
                boolean abgebrochen = false;

                while (gezahlt < drink.getPriceInCents()) {
                    System.out.println("Noch " + CurrencyFormatter.toEuroFormat(drink.getPriceInCents() - gezahlt)
                            + " nötig (0 = Abbruch).");
                    System.out.print("Münze in Cent: ");
                    int cent = readInt(scanner);
                    if (cent == 0) {
                        abgebrochen = true;
                        break;
                    }
                    if (!Muenzen.isValid(cent)) {
                        System.out.println("Ungültige Münze.");
                        continue;
                    }
                    Muenzen muenze = Muenzen.fromCents(cent);
                    einwurf.merge(muenze, 1, Integer::sum);
                    gezahlt += muenze.getValueInCents();
                }

                if (abgebrochen) {
                    if (!einwurf.isEmpty()) {
                        System.out.println("Rückgabe:");
                        einwurf.forEach((m, n) -> System.out.println("  " + n + "x " + m.getDescription()));
                    }
                    System.out.println("Abgebrochen.");
                    abgeschlossen = true;
                } else if (verkaufe(drink, einwurf)) {
                    abgeschlossen = true;
                } else {
                    System.out.println("Rückgabe:");
                    einwurf.forEach((m, n) -> System.out.println("  " + n + "x " + m.getDescription()));
                    if (i < 3) {
                        System.out.println("Bitte passend einwerfen. (Versuch " + i + "/3)");
                    } else {
                        System.out.println("Kauf abgebrochen.");
                    }
                }
            }
        }

        System.out.println("Auf Wiedersehen!");
    }

    public int getStockQuantity(Drinks drink) {
        return inventory.getStockQuantity(drink);
    }

    public void setStockQuantity(Drinks drink, int quantity) {
        inventory.setStockQuantity(drink, quantity);
    }

    public int getCoinCount(Muenzen coin) {
        return inventory.getCoinCount(coin);
    }

    public void setCoinCount(Muenzen coin, int count) {
        inventory.setCoinCount(coin, count);
    }

    private static int readInt(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
