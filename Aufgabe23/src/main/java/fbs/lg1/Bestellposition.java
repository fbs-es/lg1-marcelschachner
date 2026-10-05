package fbs.lg1;

/** Eine Position einer {@link Bestellung} mit historischem Einzelpreis. */
public class Bestellposition {

    private final int positionsnummer;
    private final int menge;
    private final double einzelpreis;
    private final Produkt produkt;

    /**
     * Legt eine Bestellposition an.
     *
     * @throws IllegalArgumentException wenn eine Angabe fehlt oder ungültig ist
     */
    public Bestellposition(int positionsnummer, int menge, double einzelpreis, Produkt produkt) {
        if (positionsnummer <= 0) {
            throw new IllegalArgumentException("Positionsnummer muss größer als 0 sein!");
        }
        if (menge <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein!");
        }
        if (einzelpreis <= 0) {
            throw new IllegalArgumentException("Einzelpreis muss größer als 0 sein!");
        }
        if (produkt == null) {
            throw new IllegalArgumentException("Produkt darf nicht fehlen!");
        }

        this.positionsnummer = positionsnummer;
        this.menge = menge;
        this.einzelpreis = einzelpreis;
        this.produkt = produkt;

        checkInvariant();
    }

    /**
     * Berechnet Menge mal Einzelpreis.
     *
     * @return der Positionswert in Euro
     */
    public double berechnePositionswert() {
        return menge * einzelpreis;
    }

    private void checkInvariant() {
        if (positionsnummer <= 0) {
            throw new IllegalStateException("Positionsnummer muss größer als 0 sein!");
        }
        if (menge <= 0) {
            throw new IllegalStateException("Menge muss größer als 0 sein!");
        }
        if (einzelpreis <= 0) {
            throw new IllegalStateException("Einzelpreis muss größer als 0 sein!");
        }
        if (produkt == null) {
            throw new IllegalStateException("Produkt darf nicht fehlen!");
        }
    }

    /** @return die Positionsnummer innerhalb der Bestellung */
    public int showPositionsnummer() {
        return positionsnummer;
    }

    /** @return die bestellte Menge */
    public int showMenge() {
        return menge;
    }

    /** @return der Einzelpreis zum Bestellzeitpunkt */
    public double showEinzelpreis() {
        return einzelpreis;
    }

    /** @return das bestellte Produkt */
    public Produkt showProdukt() {
        return produkt;
    }
}
