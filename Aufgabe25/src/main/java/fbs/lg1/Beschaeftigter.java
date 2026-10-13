package fbs.lg1;

/**
 * Abstrakte Basisklasse für alle Beschäftigten der Mitarbeiterverwaltung.
 *
 * <p>Jeder Beschäftigte hat Vor- und Nachname sowie ein nicht negatives Gehalt. Wie das Gehalt
 * angepasst wird, legt jede Unterklasse in {@link #gehaltAnpassen()} selbst fest.
 */
public abstract class Beschaeftigter {

    private final String vorname;
    private final String nachname;
    private double gehalt;

    /**
     * Legt einen Beschäftigten mit Namen und Startgehalt an.
     *
     * @throws IllegalArgumentException wenn ein Name leer oder das Gehalt negativ ist
     */
    public Beschaeftigter(String vorname, String nachname, double gehalt) {
        if (vorname == null || vorname.isBlank()) {
            throw new IllegalArgumentException("Vorname darf nicht leer sein!");
        }
        if (nachname == null || nachname.isBlank()) {
            throw new IllegalArgumentException("Nachname darf nicht leer sein!");
        }
        if (gehalt < 0) {
            throw new IllegalArgumentException("Gehalt darf nicht negativ sein!");
        }

        this.vorname = vorname;
        this.nachname = nachname;
        this.gehalt = gehalt;

        checkInvariant();
    }

    /**
     * Passt das Gehalt nach den Regeln der jeweiligen Beschäftigungsart an.
     */
    public abstract void gehaltAnpassen();

    /**
     * Setzt das Gehalt auf einen neuen Wert. Wird von den Unterklassen in
     * {@link #gehaltAnpassen()} verwendet.
     *
     * @param neuesGehalt das neue Gehalt in Euro
     * @throws IllegalArgumentException wenn das neue Gehalt negativ ist
     */
    protected void aendereGehalt(double neuesGehalt) {
        if (neuesGehalt < 0) {
            throw new IllegalArgumentException("Gehalt darf nicht negativ sein!");
        }

        gehalt = neuesGehalt;

        assert gehalt == neuesGehalt : "Fehler: Gehalt wurde nicht geändert!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (vorname == null || vorname.isBlank()) {
            throw new IllegalStateException("Vorname darf nicht leer sein!");
        }
        if (nachname == null || nachname.isBlank()) {
            throw new IllegalStateException("Nachname darf nicht leer sein!");
        }
        if (gehalt < 0) {
            throw new IllegalStateException("Gehalt darf nicht negativ sein!");
        }
    }

    /**
     * @return der Vorname
     */
    public String showVorname() {
        return vorname;
    }

    /**
     * @return der Nachname
     */
    public String showNachname() {
        return nachname;
    }

    /**
     * @return das aktuelle Gehalt in Euro
     */
    public double showGehalt() {
        return gehalt;
    }
}
