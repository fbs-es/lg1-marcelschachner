package fbs.lg1;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Demoprogramm für die Produktbewertungen.
 *
 * <p>Legt Hersteller, Produkte, Kunden und Käufe an, gibt Bewertungen ab, beantwortet eine Bewertung,
 * zeigt eine abgelehnte zweite Antwort und gibt zum Schluss die Durchschnittssterne aus. Alle
 * Ausgaben erfolgen auf Deutsch.
 */
public class App {
    /**
     * Startet die Demo.
     *
     * @param args wird nicht verwendet
     */
    public static void main(String[] args) {
        Hersteller logitech = new Hersteller("Logitech", "support@logitech.com");
        Produkt maus = new Produkt("P-001", "MX Master 3S", new BigDecimal("99.99"), logitech);
        Produkt tastatur = new Produkt("P-002", "MX Keys", new BigDecimal("119.00"), logitech);
        Kunde marcel = new Kunde("K-001", "Marcel Schachner");
        Kunde max = new Kunde("K-002", "Max Mustermann");

        LocalDate kaufDatum = LocalDate.now().minusDays(7);
        Kauf mausKauf = new Kauf(marcel, maus, kaufDatum);
        Kauf tastaturKauf = new Kauf(marcel, tastatur, kaufDatum);

        Bewertung bewertung =
                new Bewertung(marcel, maus, 5, "Beste Maus, die ich je hatte!", mausKauf);
        Bewertung ohneKauf = new Bewertung(max, maus, 3, "Etwas zu groß für meine Hand.", null);
        new Bewertung(marcel, tastatur, 4, "Schönes Tippgefühl.", tastaturKauf);
        System.out.println(
                marcel.showName()
                        + " bewertet "
                        + maus.showName()
                        + " mit "
                        + bewertung.showSterne()
                        + " Sternen: "
                        + bewertung.showKommentar());
        System.out.println(
                "Gekauft am "
                        + bewertung.showKauf().showKaufDatum()
                        + ", verifizierter Kauf: "
                        + bewertung.istGekauft());
        System.out.println(
                max.showName() + " hat das Produkt gekauft: " + ohneKauf.istGekauft());

        try {
            new Bewertung(marcel, maus, 1, "Doppelt bewertet", mausKauf);
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        Antwort antwort = bewertung.antworten("Vielen Dank für Ihre Bewertung!");
        System.out.println(
                "Antwort von "
                        + maus.showHersteller().showName()
                        + " am "
                        + antwort.showAntwortDatum()
                        + ": "
                        + antwort.showText());

        try {
            bewertung.antworten("Noch eine Antwort");
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        System.out.printf("%s: %.2f Sterne%n", maus.showName(), maus.berechneDurchschnittssterne());
        System.out.printf(
                "%s: %.2f Sterne%n", tastatur.showName(), tastatur.berechneDurchschnittssterne());
    }
}
