package fbs.lg1;

/**
 * Abstrakte Basisklasse für alle Beiträge einer {@link Radiosendung}.
 *
 * <p>Jeder Beitrag hat einen Titel, eine Dauer in Sekunden und eine Startzeit in Sekunden ab
 * Sendungsbeginn. Die Startzeit wird von der Sendung verwaltet.
 */
public abstract class Radiobeitrag {

    private final String titel;
    private int dauer;
    private int startzeit;

    /**
     * Legt einen Beitrag mit Startzeit 0 an.
     *
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
        this.startzeit = 0;

        checkInvariant();
    }

    /**
     * Erzeugt einen neuen Beitrag gleicher Art und mit gleichen Daten, aber mit anderer Dauer. Wird
     * beim Aufteilen eines Beitrags für eine {@link Eilmeldung} verwendet.
     *
     * @param dauer die Dauer der Kopie in Sekunden
     * @return die Kopie mit Startzeit 0
     * @throws IllegalArgumentException wenn die Dauer nicht größer als 0 ist
     */
    public abstract Radiobeitrag kopieMitDauer(int dauer);

    /**
     * Setzt die Dauer auf einen neuen Wert.
     *
     * @param dauer die neue Dauer in Sekunden
     * @throws IllegalArgumentException wenn die Dauer nicht größer als 0 ist
     */
    public void aendereDauer(int dauer) {
        if (dauer <= 0) {
            throw new IllegalArgumentException("Dauer muss größer als 0 sein!");
        }

        this.dauer = dauer;

        assert this.dauer == dauer : "Fehler: Dauer wurde nicht geändert!";
        checkInvariant();
    }

    /**
     * Setzt die Startzeit auf einen neuen Wert.
     *
     * @param startzeit die neue Startzeit in Sekunden ab Sendungsbeginn
     * @throws IllegalArgumentException wenn die Startzeit negativ ist
     */
    public void aendereStartzeit(int startzeit) {
        if (startzeit < 0) {
            throw new IllegalArgumentException("Startzeit darf nicht negativ sein!");
        }

        this.startzeit = startzeit;

        assert this.startzeit == startzeit : "Fehler: Startzeit wurde nicht geändert!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (titel == null || titel.isBlank()) {
            throw new IllegalStateException("Titel darf nicht leer sein!");
        }
        if (dauer <= 0) {
            throw new IllegalStateException("Dauer muss größer als 0 sein!");
        }
        if (startzeit < 0) {
            throw new IllegalStateException("Startzeit darf nicht negativ sein!");
        }
    }

    /**
     * @return der Titel
     */
    public String showTitel() {
        return titel;
    }

    /**
     * @return die Dauer in Sekunden
     */
    public int showDauer() {
        return dauer;
    }

    /**
     * @return die Startzeit in Sekunden ab Sendungsbeginn
     */
    public int showStartzeit() {
        return startzeit;
    }

    /**
     * @return die Endzeit in Sekunden ab Sendungsbeginn, also Startzeit plus Dauer
     */
    public int showEndzeit() {
        return startzeit + dauer;
    }
}
