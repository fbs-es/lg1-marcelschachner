package fbs.lg1;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Ein Produkt, das genau einem {@link Hersteller} zugeordnet ist und von vielen {@link Kunde}n
 * bewertet werden kann.
 *
 * <p>Bewertungen melden sich beim Anlegen selbst beim Produkt an (siehe {@link
 * Bewertung#Bewertung(Kunde, Produkt, int, String, boolean)}).
 *
 * <p>Beispiel:
 *
 * <pre>{@code
 * Produkt maus = new Produkt("P-001", "MX Master 3S", new BigDecimal("99.99"), hersteller);
 * new Bewertung(kunde, maus, 5, "Super Maus!", true);
 * double schnitt = maus.berechneDurchschnittssterne();
 * }</pre>
 *
 * @invariant Produktnummer und Name sind nie {@code null} oder leer.
 * @invariant Der Preis ist nie {@code null} und größer als 0.
 * @invariant Der Hersteller ist nie {@code null}.
 * @invariant Jede Bewertung in {@link #showBewertungen()} verweist auf dieses Produkt.
 * @see Hersteller
 * @see Bewertung
 */
public class Produkt {

    private final String produktNummer;
    private final String name;
    private final BigDecimal preis;
    private final Hersteller hersteller;
    private final List<Bewertung> bewertungen = new ArrayList<>();

    /**
     * Legt ein neues Produkt ohne Bewertungen an.
     *
     * @param produktNummer die Produktnummer
     * @param name der Produktname
     * @param preis der Preis in Euro
     * @param hersteller der Hersteller des Produkts
     * @pre {@code produktNummer} und {@code name} sind nicht {@code null} und nicht leer.
     * @pre {@code preis} ist nicht {@code null} und größer als 0.
     * @pre {@code hersteller} ist nicht {@code null}.
     * @post Alle Attribute sind gesetzt, {@link #showBewertungen()} ist leer.
     * @throws IllegalArgumentException wenn eine Vorbedingung verletzt ist
     */
    public Produkt(String produktNummer, String name, BigDecimal preis, Hersteller hersteller) {
        if (produktNummer == null || produktNummer.isBlank()) {
            throw new IllegalArgumentException("Produktnummer darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Produktname darf nicht leer sein!");
        }
        if (preis == null || preis.signum() <= 0) {
            throw new IllegalArgumentException("Preis muss größer als 0 sein!");
        }
        if (hersteller == null) {
            throw new IllegalArgumentException("Hersteller darf nicht fehlen!");
        }

        this.produktNummer = produktNummer;
        this.name = name;
        this.preis = preis;
        this.hersteller = hersteller;

        checkInvariant();
    }

    /**
     * Berechnet die durchschnittliche Sternebewertung aller Bewertungen dieses Produkts.
     *
     * @return der Durchschnitt der Sterne; {@code 0.0}, wenn noch keine Bewertung existiert
     * @post Ohne Bewertungen ist das Ergebnis {@code 0.0}.
     * @post Mit Bewertungen liegt das Ergebnis zwischen 1.0 und 5.0 und entspricht der Summe aller
     *     Sterne geteilt durch die Anzahl der Bewertungen.
     * @post Der Zustand des Produkts ist unverändert.
     */
    public double berechneDurchschnittssterne() {
        if (bewertungen.isEmpty()) {
            return 0.0;
        }

        int summe = 0;
        for (Bewertung bewertung : bewertungen) {
            summe += bewertung.showSterne();
        }
        double durchschnitt = (double) summe / bewertungen.size();

        assert durchschnitt >= 1.0 && durchschnitt <= 5.0
                : "Fehler: Durchschnitt liegt außerhalb von 1 bis 5!";
        checkInvariant();
        return durchschnitt;
    }

    /**
     * Fügt eine Bewertung hinzu. Wird nur vom Konstruktor von {@link Bewertung} aufgerufen.
     *
     * @param bewertung die neue Bewertung
     * @pre {@code bewertung} ist nicht {@code null} und verweist auf dieses Produkt.
     * @post {@link #showBewertungen()} enthält {@code bewertung}, die Anzahl ist um 1 gestiegen.
     * @throws IllegalArgumentException wenn eine Vorbedingung verletzt ist
     */
    void bewertungHinzufuegen(Bewertung bewertung) {
        if (bewertung == null || bewertung.showProdukt() != this) {
            throw new IllegalArgumentException("Bewertung gehört nicht zu diesem Produkt!");
        }

        int oldAnzahl = bewertungen.size();

        bewertungen.add(bewertung);

        assert bewertungen.size() == oldAnzahl + 1 : "Fehler: Bewertung wurde nicht gespeichert!";
        checkInvariant();
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn eine Invariante verletzt ist
     */
    private void checkInvariant() {
        if (produktNummer == null || produktNummer.isBlank()) {
            throw new IllegalStateException("Produktnummer darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Produktname darf nicht leer sein!");
        }
        if (preis == null || preis.signum() <= 0) {
            throw new IllegalStateException("Preis muss größer als 0 sein!");
        }
        if (hersteller == null) {
            throw new IllegalStateException("Hersteller darf nicht fehlen!");
        }
        for (Bewertung bewertung : bewertungen) {
            if (bewertung.showProdukt() != this) {
                throw new IllegalStateException("Bewertung gehört zu einem anderen Produkt!");
            }
        }
    }

    /**
     * Liefert die Produktnummer.
     *
     * @return die Produktnummer, nie {@code null} oder leer
     */
    public String showProduktNummer() {
        return produktNummer;
    }

    /**
     * Liefert den Produktnamen.
     *
     * @return der Name, nie {@code null} oder leer
     */
    public String showName() {
        return name;
    }

    /**
     * Liefert den Preis.
     *
     * @return der Preis in Euro, immer größer als 0
     */
    public BigDecimal showPreis() {
        return preis;
    }

    /**
     * Liefert den Hersteller.
     *
     * @return der Hersteller, nie {@code null}
     */
    public Hersteller showHersteller() {
        return hersteller;
    }

    /**
     * Liefert alle Bewertungen dieses Produkts, älteste zuerst.
     *
     * <p>Die Liste ist die interne Liste des Produkts und keine Kopie. Sie darf nicht verändert
     * werden.
     *
     * @return alle Bewertungen, nie {@code null}
     */
    public List<Bewertung> showBewertungen() {
        return bewertungen;
    }
}
