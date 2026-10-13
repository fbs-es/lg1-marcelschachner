package fbs.lg1;

import java.time.LocalDate;

public class App {

    public static void main(String[] args) {
        Angestellter angestellter = new Angestellter("Marcel", "Schachner", 3000.0, 2020, 25);
        Auszubildender auszubildender =
                new Auszubildender("Max", "Mustermann", 900.0, "Informatiker", 2);
        Aushilfe aushilfe = new Aushilfe("Marcel", "Schachner", 0.0);

        System.out.printf(
                "Angestellter %s %s: Gehalt %.2f €, eingestellt %d, Urlaub %d Tage%n",
                angestellter.showVorname(),
                angestellter.showNachname(),
                angestellter.showGehalt(),
                angestellter.showEinstellungsjahr(),
                angestellter.showUrlaubsanspruch());
        angestellter.gehaltAnpassen();
        System.out.printf("Nach Anpassung: %.2f €%n", angestellter.showGehalt());

        System.out.printf(
                "Auszubildender %s %s: %s im %d. Jahr, Gehalt %.2f €%n",
                auszubildender.showVorname(),
                auszubildender.showNachname(),
                auszubildender.showAusbildungsberuf(),
                auszubildender.showAusbildungsjahr(),
                auszubildender.showGehalt());
        auszubildender.gehaltAnpassen();
        System.out.printf("Nach Anpassung: %.2f €%n", auszubildender.showGehalt());

        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 1), 120);
        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 2), 90);
        System.out.printf(
                "Aushilfe %s %s: %d Arbeitszeiten erfasst, Gehalt %.2f €%n",
                aushilfe.showVorname(),
                aushilfe.showNachname(),
                aushilfe.showArbeitszeiten().size(),
                aushilfe.showGehalt());
        aushilfe.gehaltAnpassen();
        System.out.printf("Nach Abrechnung: %.2f €%n", aushilfe.showGehalt());
        for (Arbeitszeit arbeitszeit : aushilfe.showArbeitszeiten()) {
            System.out.println(
                    "  "
                            + arbeitszeit.showDatum()
                            + ": "
                            + arbeitszeit.showMinuten()
                            + " Minuten, abgerechnet: "
                            + arbeitszeit.istAbgerechnet());
        }

        try {
            aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 3), 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        try {
            new Auszubildender("Max", "Mustermann", 900.0, "Informatiker", 5);
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }
}
