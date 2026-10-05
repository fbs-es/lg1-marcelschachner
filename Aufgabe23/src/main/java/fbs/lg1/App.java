package fbs.lg1;

import java.util.Date;

/**
 * Demoprogramm für die Bestellverwaltung.
 *
 * <p>Legt Lieferant, Produkte und Kunden an, erstellt Bestellungen mit mehreren Positionen, ändert
 * einen Preis, versendet und storniert Bestellungen, zeigt abgelehnte Aktionen und prüft zum
 * Schluss den Nachschubbedarf. Alle Ausgaben erfolgen auf Deutsch.
 */
public class App {

    /**
     * Startet die Demo.
     *
     * @param args wird nicht verwendet
     */
    public static void main(String[] args) {
        Date heute = new Date();
        Lieferant lieferant = new Lieferant(1, "Logitech");
        Produkt maus = new Produkt(1, "MX Master 3S", 99.99, 20, 5, lieferant);
        Produkt tastatur = new Produkt(2, "MX Keys", 119.00, 3, 5, lieferant);
        Kunde marcel = new Kunde(1, "Marcel Schachner", "schachner.marcel@icloud.com");
        Kunde max = new Kunde(2, "Max Mustermann", "max.mustermann@gmail.com");

        Bestellung bestellungMarcel = new Bestellung(1, heute, marcel, maus, 2);
        bestellungMarcel.positionHinzufuegen(tastatur, 1);
        System.out.println(
                "Bestellung "
                        + bestellungMarcel.showBestellnummer()
                        + " von "
                        + marcel.showName()
                        + ": "
                        + bestellungMarcel.showPositionen().size()
                        + " Positionen, Gesamtwert "
                        + bestellungMarcel.berechneGesamtwert()
                        + " €");

        maus.preisAendern(89.99);
        System.out.println(
                "Neuer Preis für " + maus.showBezeichnung() + ": " + maus.showEinzelpreis() + " €");

        Bestellung bestellungMax = new Bestellung(2, heute, max, maus, 1);
        System.out.println(
                "Bestellung "
                        + bestellungMax.showBestellnummer()
                        + " von "
                        + max.showName()
                        + ": Gesamtwert "
                        + bestellungMax.berechneGesamtwert()
                        + " €");
        System.out.println(
                "Bestellung "
                        + bestellungMarcel.showBestellnummer()
                        + " bleibt bei "
                        + bestellungMarcel.berechneGesamtwert()
                        + " €");

        bestellungMarcel.versenden();
        System.out.println(
                "Bestellung "
                        + bestellungMarcel.showBestellnummer()
                        + ": "
                        + bestellungMarcel.showStatus());

        try {
            bestellungMarcel.positionHinzufuegen(maus, 1);
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        System.out.println(
                "Bestellung "
                        + bestellungMarcel.showBestellnummer()
                        + " storniert: "
                        + bestellungMarcel.stornieren());
        System.out.println(
                "Bestellung "
                        + bestellungMax.showBestellnummer()
                        + " storniert: "
                        + bestellungMax.stornieren());
        System.out.println(
                "Bestellung "
                        + bestellungMax.showBestellnummer()
                        + ": "
                        + bestellungMax.showStatus());

        System.out.println(
                maus.showBezeichnung() + " Nachschub nötig: " + maus.pruefeNachschubbedarf());
        System.out.println(
                tastatur.showBezeichnung()
                        + " Nachschub nötig: "
                        + tastatur.pruefeNachschubbedarf());
        System.out.println(
                "Lieferantenbestellungen für "
                        + tastatur.showBezeichnung()
                        + ": "
                        + tastatur.showLieferantenbestellungen().size());
    }
}
