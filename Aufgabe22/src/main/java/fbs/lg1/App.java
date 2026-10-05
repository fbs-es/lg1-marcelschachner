package fbs.lg1;

import java.math.BigDecimal;

/**
 * Demoprogramm für die Produktbewertungen.
 *
 * <p>Legt Hersteller, Produkte und Kunden an, gibt Bewertungen ab, beantwortet eine Bewertung,
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

        Bewertung bewertung = new Bewertung(marcel, maus, 5, "Beste Maus, die ich je hatte!", true);
        new Bewertung(max, maus, 3, "Etwas zu groß für meine Hand.", false);
        new Bewertung(marcel, tastatur, 4, "Schönes Tippgefühl.", true);
        System.out.println(
                marcel.showName()
                        + " bewertet "
                        + maus.showName()
                        + " mit "
                        + bewertung.showSterne()
                        + " Sternen: "
                        + bewertung.showKommentar());

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
