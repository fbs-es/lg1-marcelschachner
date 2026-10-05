package fbs.lg1;

import java.util.Date;

/** Eine Nachbestellung eines {@link Produkt}s beim {@link Lieferant}en. */
public class Lieferantenbestellung {

    private final int lieferantenbestellnummer;
    private final Date datum;
    private final int menge;
    private final Produkt produkt;
    private final Lieferant lieferant;

    /**
     * Legt eine Lieferantenbestellung an.
     *
     * @throws IllegalArgumentException wenn eine Angabe fehlt oder ungültig ist
     */
    public Lieferantenbestellung(
            int lieferantenbestellnummer, Date datum, int menge, Produkt produkt, Lieferant lieferant) {
        if (lieferantenbestellnummer <= 0) {
            throw new IllegalArgumentException("Lieferantenbestellnummer muss größer als 0 sein!");
        }
        if (datum == null) {
            throw new IllegalArgumentException("Datum darf nicht fehlen!");
        }
        if (menge <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein!");
        }
        if (produkt == null) {
            throw new IllegalArgumentException("Produkt darf nicht fehlen!");
        }
        if (lieferant == null) {
            throw new IllegalArgumentException("Lieferant darf nicht fehlen!");
        }

        this.lieferantenbestellnummer = lieferantenbestellnummer;
        this.datum = datum;
        this.menge = menge;
        this.produkt = produkt;
        this.lieferant = lieferant;

        checkInvariant();
    }

    private void checkInvariant() {
        if (lieferantenbestellnummer <= 0) {
            throw new IllegalStateException("Lieferantenbestellnummer muss größer als 0 sein!");
        }
        if (menge <= 0) {
            throw new IllegalStateException("Menge muss größer als 0 sein!");
        }
        if (datum == null || produkt == null || lieferant == null) {
            throw new IllegalStateException("Pflichtangaben der Lieferantenbestellung fehlen!");
        }
    }

    /** @return die Lieferantenbestellnummer */
    public int showLieferantenbestellnummer() {
        return lieferantenbestellnummer;
    }

    /** @return das Bestelldatum */
    public Date showDatum() {
        return datum;
    }

    /** @return die bestellte Menge */
    public int showMenge() {
        return menge;
    }

    /** @return das nachbestellte Produkt */
    public Produkt showProdukt() {
        return produkt;
    }

    /** @return der Lieferant */
    public Lieferant showLieferant() {
        return lieferant;
    }
}
