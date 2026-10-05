package fbs.lg1;

/**
 * Ein Kunde des Portals, der {@link Produkt}e bewerten kann.
 *
 * <p>Bewertungen werden mit {@link Bewertung#Bewertung(Kunde, Produkt, int, String, boolean)}
 * angelegt.
 *
 * @invariant Kundennummer und Name sind nie {@code null} oder leer.
 * @see Bewertung
 */
public class Kunde {

    private final String kundenNummer;
    private final String name;

    /**
     * Legt einen neuen Kunden an.
     *
     * @param kundenNummer die Kundennummer
     * @param name der Name des Kunden
     * @pre {@code kundenNummer} und {@code name} sind nicht {@code null} und nicht leer.
     * @post {@link #showKundenNummer()} liefert {@code kundenNummer}, {@link #showName()} liefert
     *     {@code name}.
     * @throws IllegalArgumentException wenn eine Vorbedingung verletzt ist
     */
    public Kunde(String kundenNummer, String name) {
        if (kundenNummer == null || kundenNummer.isBlank()) {
            throw new IllegalArgumentException("Kundennummer darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }

        this.kundenNummer = kundenNummer;
        this.name = name;

        checkInvariant();
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn eine Invariante verletzt ist
     */
    private void checkInvariant() {
        if (kundenNummer == null || kundenNummer.isBlank()) {
            throw new IllegalStateException("Kundennummer darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
    }

    /**
     * Liefert die Kundennummer.
     *
     * @return die Kundennummer, nie {@code null} oder leer
     */
    public String showKundenNummer() {
        return kundenNummer;
    }

    /**
     * Liefert den Namen des Kunden.
     *
     * @return der Name, nie {@code null} oder leer
     */
    public String showName() {
        return name;
    }
}
