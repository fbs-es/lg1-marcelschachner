package fbs.lg1;

import java.time.LocalDate;

/**
 * Ein Kauf, bei dem ein {@link Kunde} ein {@link Produkt} gekauft hat.
 *
 * <p>Eine {@link Bewertung} kann auf einen Kauf verweisen. Dann gilt das Produkt als tatsächlich
 * gekauft (siehe {@link Bewertung#istGekauft()}).
 *
 * @invariant Kunde, Produkt und Kaufdatum sind nie {@code null}.
 * @invariant Das Kaufdatum liegt nicht in der Zukunft.
 * @see Kunde
 * @see Produkt
 * @see Bewertung
 */
public class Kauf {

    private final Kunde kunde;
    private final Produkt produkt;
    private final LocalDate kaufDatum;

    /**
     * Legt einen neuen Kauf an.
     *
     * @param kunde der kaufende Kunde
     * @param produkt das gekaufte Produkt
     * @param kaufDatum das Datum des Kaufs
     * @pre {@code kunde} und {@code produkt} sind nicht {@code null}.
     * @pre {@code kaufDatum} ist nicht {@code null} und liegt nicht in der Zukunft.
     * @post Alle Attribute sind gesetzt.
     * @throws IllegalArgumentException wenn eine Vorbedingung verletzt ist
     */
    public Kauf(Kunde kunde, Produkt produkt, LocalDate kaufDatum) {
        if (kunde == null) {
            throw new IllegalArgumentException("Kunde darf nicht fehlen!");
        }
        if (produkt == null) {
            throw new IllegalArgumentException("Produkt darf nicht fehlen!");
        }
        if (kaufDatum == null) {
            throw new IllegalArgumentException("Kaufdatum darf nicht fehlen!");
        }
        if (kaufDatum.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Kaufdatum darf nicht in der Zukunft liegen!");
        }

        this.kunde = kunde;
        this.produkt = produkt;
        this.kaufDatum = kaufDatum;

        checkInvariant();
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn eine Invariante verletzt ist
     */
    private void checkInvariant() {
        if (kunde == null || produkt == null || kaufDatum == null) {
            throw new IllegalStateException("Pflichtangaben des Kaufs fehlen!");
        }
        if (kaufDatum.isAfter(LocalDate.now())) {
            throw new IllegalStateException("Kaufdatum darf nicht in der Zukunft liegen!");
        }
    }

    /**
     * Liefert den kaufenden Kunden.
     *
     * @return der Kunde, nie {@code null}
     */
    public Kunde showKunde() {
        return kunde;
    }

    /**
     * Liefert das gekaufte Produkt.
     *
     * @return das Produkt, nie {@code null}
     */
    public Produkt showProdukt() {
        return produkt;
    }

    /**
     * Liefert das Kaufdatum.
     *
     * @return das Datum, nie {@code null}
     */
    public LocalDate showKaufDatum() {
        return kaufDatum;
    }
}
