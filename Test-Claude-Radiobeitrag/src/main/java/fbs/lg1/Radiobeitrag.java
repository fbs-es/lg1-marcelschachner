package fbs.lg1;

/**
 * Abstrakte Basisklasse für alle Beiträge im Sendeplan einer {@link Radiosendung}.
 *
 * <p>Jeder Beitrag hat einen Titel, eine geplante Dauer in Sekunden und einen Startzeitpunkt in
 * Sekunden relativ zum Beginn der Sendung. Der Startzeitpunkt wird von der Sendung berechnet und
 * nicht von außen eingegeben.
 */
public abstract class Radiobeitrag {

    private final String titel;
    private int dauer;
    private int startzeitpunkt;

    /**
     * Legt einen Beitrag mit Titel und geplanter Dauer an. Der Startzeitpunkt ist anfangs 0.
     *
     * @param titel der Titel des Beitrags
     * @param dauer die geplante Dauer in Sekunden
     * @throws IllegalArgumentException wenn der Titel leer oder die Dauer nicht größer als 0 ist
     */
    public Radiobeitrag(String titel, int dauer) {
        if (titel == null || titel.isBlank()) {
            throw new IllegalArgumentException("Titel darf nicht leer sein!");
        }
        if (dauer <= 0) {
            throw new IllegalArgumentException("Dauer muss größer als 0 sein!");
        }

        this.titel = titel;
        this.dauer = dauer;
        this.startzeitpunkt = 0;

        checkInvariant();
    }

    /**
     * Erstellt einen neuen Beitrag derselben Kategorie, der den Rest dieses Beitrags nach einer
     * Unterbrechung enthält.
     *
     * @param restdauer die verbleibende Dauer in Sekunden
     * @return der neue Fortsetzungsbeitrag
     */
    protected abstract Radiobeitrag erstelleFortsetzung(int restdauer);

    /**
     * Ändert die geplante Dauer. Wird nur von der {@link Radiosendung} verwendet, damit die
     * Startzeitpunkte danach neu berechnet werden.
     *
     * @param neueDauer die neue Dauer in Sekunden
     * @throws IllegalArgumentException wenn die neue Dauer nicht größer als 0 ist
     */
    void aendereDauer(int neueDauer) {
        if (neueDauer <= 0) {
            throw new IllegalArgumentException("Dauer muss größer als 0 sein!");
        }

        dauer = neueDauer;

        assert dauer == neueDauer : "Fehler: Dauer wurde nicht geändert!";
        checkInvariant();
    }

    /**
     * Ändert den Startzeitpunkt. Wird nur von der {@link Radiosendung} verwendet.
     *
     * @param neuerStartzeitpunkt der neue Startzeitpunkt in Sekunden ab Sendungsbeginn
     * @throws IllegalArgumentException wenn der Startzeitpunkt negativ ist
     */
    void aendereStartzeitpunkt(int neuerStartzeitpunkt) {
        if (neuerStartzeitpunkt < 0) {
            throw new IllegalArgumentException("Startzeitpunkt darf nicht negativ sein!");
        }

        startzeitpunkt = neuerStartzeitpunkt;

        assert startzeitpunkt == neuerStartzeitpunkt : "Fehler: Startzeitpunkt wurde nicht geändert!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (titel == null || titel.isBlank()) {
            throw new IllegalStateException("Titel darf nicht leer sein!");
        }
        if (dauer <= 0) {
            throw new IllegalStateException("Dauer muss größer als 0 sein!");
        }
        if (startzeitpunkt < 0) {
            throw new IllegalStateException("Startzeitpunkt darf nicht negativ sein!");
        }
    }

    /**
     * @return der Titel des Beitrags
     */
    public String showTitel() {
        return titel;
    }

    /**
     * @return die geplante Dauer in Sekunden
     */
    public int showDauer() {
        return dauer;
    }

    /**
     * @return der Startzeitpunkt in Sekunden ab Sendungsbeginn
     */
    public int showStartzeitpunkt() {
        return startzeitpunkt;
    }
}
