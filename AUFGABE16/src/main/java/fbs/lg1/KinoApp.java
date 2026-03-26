package fbs.lg1;

import java.util.Scanner;

public class KinoApp {
    private final KinoVerwaltung kino = new KinoVerwaltung();

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            starten(scanner);
        }
    }

    private void starten(Scanner scanner) {
        while (true) {
            zeigeUebersicht();
            int anzahl = leseAnzahl(scanner);
            if (anzahl == 0)
                break;
            handleVormerken(scanner, anzahl);
        }
        System.out.println("Auf Wiedersehen!");
    }

    private void zeigeUebersicht() {
        zeigeKinosaal(false);
        System.out.println();
        zeigeStatistik();
        System.out.println();
    }

    private void zeigeKinosaal(boolean highlight) {
        for (int i = 0; i < kino.getReihen(); i++) {
            System.out.print("Reihe " + (i + 1) + ": ");
            for (int j = 0; j < kino.getPlaetze(); j++) {
                SitzplatzStatus status = kino.getSitzplatzStatus(i, j);
                System.out.print(
                        (highlight && status == SitzplatzStatus.VORGEMERKT) ? "[V] " : status.getSymbol() + "   ");
            }
            System.out.println();
        }
    }

    private void zeigeStatistik() {
        System.out.printf("%-12s | %6d |%n", "Frei", kino.zaehle(SitzplatzStatus.FREI));
        System.out.printf("%-12s | %6d |%n", "Reserviert", kino.zaehle(SitzplatzStatus.RESERVIERT));
        System.out.printf("%-12s | %6d |%n", "Vorgemerkt", kino.zaehle(SitzplatzStatus.VORGEMERKT));
    }

    private int leseAnzahl(Scanner scanner) {
        int anzahl;
        do {
            System.out.print("Anzahl Sitze (1-3, 0 = Beenden): ");
            anzahl = scanner.nextInt();
            if (anzahl < 0 || anzahl > 3)
                System.out.println("Ungültige Eingabe. Bitte 0 bis 3 eingeben.");
        } while (anzahl < 0 || anzahl > 3);
        return anzahl;
    }

    private void handleVormerken(Scanner scanner, int anzahl) {
        System.out.print("Ab einer bestimmten Reihe suchen? (j/n): ");
        String mitStart = scanner.next();
        int[][] positionen;

        if (mitStart.equalsIgnoreCase("j")) {
            System.out.print("Ab Reihe (1-" + kino.getReihen() + "): ");
            int reihe = scanner.nextInt() - 1;
            System.out.print("Ab Sitz (1-" + kino.getPlaetze() + "): ");
            int sitz = scanner.nextInt() - 1;
            positionen = kino.vormerken(anzahl, reihe, sitz);
        } else {
            positionen = kino.vormerken(anzahl);
        }

        if (positionen.length == 0) {
            System.out.println("Keine " + anzahl + " Plätze hintereinander verfügbar");
            return;
        }

        System.out.println();
        zeigeKinosaal(true);
        System.out.println();
        System.out.print("Plätze reservieren? (j/n): ");
        String antwort = scanner.next();
        if (antwort.equalsIgnoreCase("j")) {
            kino.bestaetigen(positionen);
            System.out.println("Plätze wurden reserviert.");
        } else {
            kino.ablehnen(positionen);
            System.out.println("Reservierung abgebrochen.");
        }
        System.out.println();
    }
}
