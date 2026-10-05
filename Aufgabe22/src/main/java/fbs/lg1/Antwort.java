package fbs.lg1;

import java.time.LocalDate;

/**
 * Die Antwort eines Verkäufers auf genau eine {@link Bewertung}.
 *
 * <p>Antworten werden über {@link Bewertung#antworten(String)} angelegt und sind danach
 * unveränderlich.
 *
 * @invariant Bewertung und Antwortdatum sind nie {@code null}.
 * @invariant Der Antworttext ist nie {@code null} oder leer.
 * @see Bewertung
 */
public class Antwort {

    private final Bewertung bewertung;
    private final String text;
    private final LocalDate antwortDatum;

    /**
     * Legt eine neue Antwort mit dem heutigen Datum an. Wird nur von {@link
     * Bewertung#antworten(String)} aufgerufen.
     *
     * @param bewertung die beantwortete Bewertung
     * @param text der Antworttext
     * @pre {@code bewertung} ist nicht {@code null}.
     * @pre {@code text} ist nicht {@code null} und nicht leer.
     * @post Alle Attribute sind gesetzt, das Antwortdatum ist das heutige Datum.
     * @throws IllegalArgumentException wenn eine Vorbedingung verletzt ist
     */
    Antwort(Bewertung bewertung, String text) {
        if (bewertung == null) {
            throw new IllegalArgumentException("Bewertung darf nicht fehlen!");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Antworttext darf nicht leer sein!");
        }

        this.bewertung = bewertung;
        this.text = text;
        this.antwortDatum = LocalDate.now();

        checkInvariant();
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn eine Invariante verletzt ist
     */
    private void checkInvariant() {
        if (bewertung == null || antwortDatum == null) {
            throw new IllegalStateException("Pflichtangaben der Antwort fehlen!");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Antworttext darf nicht leer sein!");
        }
    }

    /**
     * Liefert die beantwortete Bewertung.
     *
     * @return die Bewertung, nie {@code null}
     */
    public Bewertung showBewertung() {
        return bewertung;
    }

    /**
     * Liefert den Antworttext.
     *
     * @return der Text, nie {@code null} oder leer
     */
    public String showText() {
        return text;
    }

    /**
     * Liefert das Antwortdatum.
     *
     * @return das Datum, nie {@code null}
     */
    public LocalDate showAntwortDatum() {
        return antwortDatum;
    }
}
