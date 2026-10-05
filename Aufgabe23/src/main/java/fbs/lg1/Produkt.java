package fbs.lg1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Ein Produkt mit Lagerbestand, das bei Bedarf beim {@link Lieferant}en nachbestellt wird. */
public class Produkt {

    private final int produktnr;
    private final String bezeichnung;
    private final int mindestbestand;
    private final Lieferant lieferant;
    private final List<Lieferantenbestellung> lieferantenbestellungen = new ArrayList<>();
    private double einzelpreis;
    private int lagerbestand;

    /**
     * Legt ein Produkt an.
     *
     * @throws IllegalArgumentException wenn eine Angabe fehlt oder ungültig ist
     */
    public Produkt(
            int produktnr,
            String bezeichnung,
            double einzelpreis,
            int lagerbestand,
            int mindestbestand,
            Lieferant lieferant) {
        if (produktnr <= 0) {
            throw new IllegalArgumentException("Produktnummer muss größer als 0 sein!");
        }
        if (bezeichnung == null || bezeichnung.isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein!");
        }
        if (einzelpreis <= 0) {
            throw new IllegalArgumentException("Einzelpreis muss größer als 0 sein!");
        }
        if (lagerbestand < 0) {
            throw new IllegalArgumentException("Lagerbestand darf nicht negativ sein!");
        }
        if (mindestbestand <= 0) {
            throw new IllegalArgumentException("Mindestbestand muss größer als 0 sein!");
        }
        if (lieferant == null) {
            throw new IllegalArgumentException("Lieferant darf nicht fehlen!");
        }

        this.produktnr = produktnr;
        this.bezeichnung = bezeichnung;
        this.einzelpreis = einzelpreis;
        this.lagerbestand = lagerbestand;
        this.mindestbestand = mindestbestand;
        this.lieferant = lieferant;

        checkInvariant();
    }

    /**
     * Löst eine Lieferantenbestellung aus, wenn der Lagerbestand den Mindestbestand erreicht oder unterschreitet.
     *
     * @return {@code true}, wenn nachbestellt wurde
     */
    public boolean pruefeNachschubbedarf() {
        if (lagerbestand > mindestbestand) {
            return false;
        }

        int oldAnzahl = lieferantenbestellungen.size();

        nachbestellen();

        assert lieferantenbestellungen.size() == oldAnzahl + 1
                : "Fehler: Lieferantenbestellung wurde nicht ausgelöst!";
        checkInvariant();
        return true;
    }

    /**
     * Ändert den aktuellen Einzelpreis. Bestehende Positionen bleiben unverändert.
     *
     * @param neuerPreis der neue Preis in Euro
     * @throws IllegalArgumentException wenn der Preis nicht größer als 0 ist
     */
    public void preisAendern(double neuerPreis) {
        if (neuerPreis <= 0) {
            throw new IllegalArgumentException("Einzelpreis muss größer als 0 sein!");
        }

        einzelpreis = neuerPreis;

        assert einzelpreis == neuerPreis : "Fehler: Preis wurde nicht geändert!";
        checkInvariant();
    }

    private Lieferantenbestellung nachbestellen() {
        int nummer = lieferantenbestellungen.size() + 1;
        int bestellmenge = mindestbestand * 2 - lagerbestand;
        Date datum = new Date();
        Lieferantenbestellung lieferantenbestellung =
                new Lieferantenbestellung(nummer, datum, bestellmenge, this, lieferant);
        lieferantenbestellungen.add(lieferantenbestellung);
        System.out.println(
                "Lieferantenbestellung "
                        + nummer
                        + " über "
                        + bestellmenge
                        + " Stück "
                        + bezeichnung
                        + " bei "
                        + lieferant.showName()
                        + " ausgelöst.");
        return lieferantenbestellung;
    }

    private void checkInvariant() {
        if (produktnr <= 0) {
            throw new IllegalStateException("Produktnummer muss größer als 0 sein!");
        }
        if (bezeichnung == null || bezeichnung.isBlank()) {
            throw new IllegalStateException("Bezeichnung darf nicht leer sein!");
        }
        if (einzelpreis <= 0) {
            throw new IllegalStateException("Einzelpreis muss größer als 0 sein!");
        }
        if (lagerbestand < 0) {
            throw new IllegalStateException("Lagerbestand darf nicht negativ sein!");
        }
        if (mindestbestand <= 0) {
            throw new IllegalStateException("Mindestbestand muss größer als 0 sein!");
        }
        if (lieferant == null) {
            throw new IllegalStateException("Lieferant darf nicht fehlen!");
        }
        int erwartet = 1;
        for (Lieferantenbestellung lieferantenbestellung : lieferantenbestellungen) {
            if (lieferantenbestellung.showProdukt() != this) {
                throw new IllegalStateException(
                        "Lieferantenbestellung gehört zu einem anderen Produkt!");
            }
            if (lieferantenbestellung.showLieferantenbestellnummer() != erwartet) {
                throw new IllegalStateException("Lieferantenbestellungen sind nicht lückenlos!");
            }
            erwartet++;
        }
    }

    /** @return die Produktnummer */
    public int showProduktnr() {
        return produktnr;
    }

    /** @return die Bezeichnung */
    public String showBezeichnung() {
        return bezeichnung;
    }

    /** @return der aktuelle Einzelpreis */
    public double showEinzelpreis() {
        return einzelpreis;
    }

    /** @return der Lagerbestand */
    public int showLagerbestand() {
        return lagerbestand;
    }

    /** @return der Mindestbestand */
    public int showMindestbestand() {
        return mindestbestand;
    }

    /** @return der Lieferant */
    public Lieferant showLieferant() {
        return lieferant;
    }

    /** @return alle Lieferantenbestellungen, lückenlos nummeriert, nicht verändern */
    public List<Lieferantenbestellung> showLieferantenbestellungen() {
        return lieferantenbestellungen;
    }
}
