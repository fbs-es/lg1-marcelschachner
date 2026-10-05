package fbs.lg1;

import java.time.LocalDate;

/**
 * Eine Bewertung, die einen {@link Kunde}n mit einem {@link Produkt} verknüpft.
 *
 * <p>Ein Verkäufer kann mit {@link #antworten(String)} höchstens einmal darauf antworten.
 *
 * <p>Beispiel:
 *
 * <pre>{@code
 * Bewertung bewertung = new Bewertung(kunde, produkt, 4, "Gutes Produkt", true);
 * bewertung.antworten("Vielen Dank!");
 * }</pre>
 *
 * @invariant Kunde, Produkt und Erstellungsdatum sind nie {@code null}.
 * @invariant Die Anzahl der Sterne liegt zwischen 1 und 5.
 * @invariant Falls eine Antwort existiert, verweist sie auf diese Bewertung.
 * @see Kunde
 * @see Produkt
 * @see Antwort
 */
public class Bewertung {

    private final Kunde kunde;
    private final Produkt produkt;
    private final int sterne;
    private final String kommentar;
    private final LocalDate erstellungsDatum;
    private final boolean gekauft;
    private Antwort antwort;

    /**
     * Legt eine neue Bewertung mit dem heutigen Datum an und meldet sie beim Produkt an.
     *
     * @param kunde der bewertende Kunde
     * @param produkt das bewertete Produkt
     * @param sterne die Anzahl der Sterne
     * @param kommentar der persönliche Kommentar, optional ({@code null} erlaubt)
     * @param gekauft {@code true}, wenn der Kunde das Produkt tatsächlich gekauft hat
     * @pre {@code kunde} und {@code produkt} sind nicht {@code null}.
     * @pre {@code sterne} liegt zwischen 1 und 5.
     * @post Alle Attribute sind gesetzt, das Erstellungsdatum ist das heutige Datum.
     * @post {@code produkt.showBewertungen()} enthält diese Bewertung.
     * @post Die Bewertung hat keine Antwort.
     * @throws IllegalArgumentException wenn eine Vorbedingung verletzt ist
     */
    public Bewertung(Kunde kunde, Produkt produkt, int sterne, String kommentar, boolean gekauft) {
        if (kunde == null) {
            throw new IllegalArgumentException("Kunde darf nicht fehlen!");
        }
        if (produkt == null) {
            throw new IllegalArgumentException("Produkt darf nicht fehlen!");
        }
        if (sterne < 1 || sterne > 5) {
            throw new IllegalArgumentException("Sterne müssen zwischen 1 und 5 liegen!");
        }

        this.kunde = kunde;
        this.produkt = produkt;
        this.sterne = sterne;
        this.kommentar = kommentar;
        this.gekauft = gekauft;
        this.erstellungsDatum = LocalDate.now();

        produkt.bewertungHinzufuegen(this);

        assert produkt.showBewertungen().contains(this)
                : "Fehler: Bewertung wurde nicht beim Produkt gespeichert!";
        assert antwort == null : "Fehler: Neue Bewertung hat bereits eine Antwort!";
        checkInvariant();
    }

    /**
     * Beantwortet die Bewertung als Verkäufer. Das Antwortdatum ist das heutige Datum.
     *
     * @param text der Antworttext
     * @return die neue Antwort, nie {@code null}
     * @pre Die Bewertung hat noch keine Antwort.
     * @pre {@code text} ist nicht {@code null} und nicht leer.
     * @post {@link #showAntwort()} liefert die neue Antwort, die auf diese Bewertung verweist.
     * @throws IllegalStateException wenn die Bewertung bereits beantwortet wurde
     * @throws IllegalArgumentException wenn {@code text} leer ist
     */
    public Antwort antworten(String text) {
        if (antwort != null) {
            throw new IllegalStateException("Bewertung wurde bereits beantwortet!");
        }

        antwort = new Antwort(this, text);

        assert antwort.showBewertung() == this : "Fehler: Antwort verweist nicht auf Bewertung!";
        checkInvariant();
        return antwort;
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn eine Invariante verletzt ist
     */
    private void checkInvariant() {
        if (kunde == null || produkt == null || erstellungsDatum == null) {
            throw new IllegalStateException("Pflichtangaben der Bewertung fehlen!");
        }
        if (sterne < 1 || sterne > 5) {
            throw new IllegalStateException("Sterne müssen zwischen 1 und 5 liegen!");
        }
        if (antwort != null && antwort.showBewertung() != this) {
            throw new IllegalStateException("Antwort gehört zu einer anderen Bewertung!");
        }
    }

    /**
     * Liefert den bewertenden Kunden.
     *
     * @return der Kunde, nie {@code null}
     */
    public Kunde showKunde() {
        return kunde;
    }

    /**
     * Liefert das bewertete Produkt.
     *
     * @return das Produkt, nie {@code null}
     */
    public Produkt showProdukt() {
        return produkt;
    }

    /**
     * Liefert die Anzahl der Sterne.
     *
     * @return die Sterne, immer zwischen 1 und 5
     */
    public int showSterne() {
        return sterne;
    }

    /**
     * Liefert den Kommentar.
     *
     * @return der Kommentar, kann {@code null} sein
     */
    public String showKommentar() {
        return kommentar;
    }

    /**
     * Liefert das Erstellungsdatum.
     *
     * @return das Erstellungsdatum, nie {@code null}
     */
    public LocalDate showErstellungsDatum() {
        return erstellungsDatum;
    }

    /**
     * Prüft, ob der Kunde das Produkt tatsächlich gekauft hat.
     *
     * @return {@code true} bei einem tatsächlichen Kauf
     */
    public boolean istGekauft() {
        return gekauft;
    }

    /**
     * Liefert die Antwort des Verkäufers.
     *
     * @return die Antwort oder {@code null}, solange die Bewertung nicht beantwortet wurde
     */
    public Antwort showAntwort() {
        return antwort;
    }
}
