package fbs.lg1;

import java.time.LocalDate;

/**
 * Eine an einem Tag gearbeitete Zeit einer {@link Aushilfe} in Minuten.
 *
 * <p>Eine Arbeitszeit kann genau einmal abgerechnet werden.
 */
public class Arbeitszeit {

    private final LocalDate datum;
    private final int minuten;
    private boolean abgerechnet;

    /**
     * Legt eine noch nicht abgerechnete Arbeitszeit an.
     *
     * @throws IllegalArgumentException wenn das Datum fehlt oder die Minuten nicht größer als 0 sind
     */
    public Arbeitszeit(LocalDate datum, int minuten) {
        if (datum == null) {
            throw new IllegalArgumentException("Datum darf nicht fehlen!");
        }
        if (minuten <= 0) {
            throw new IllegalArgumentException("Minuten müssen größer als 0 sein!");
        }

        this.datum = datum;
        this.minuten = minuten;
        this.abgerechnet = false;

        checkInvariant();
    }

    /**
     * Markiert die Arbeitszeit als abgerechnet.
     *
     * @throws IllegalStateException wenn die Arbeitszeit bereits abgerechnet ist
     */
    public void abrechnen() {
        if (abgerechnet) {
            throw new IllegalStateException("Arbeitszeit ist bereits abgerechnet!");
        }

        abgerechnet = true;

        assert abgerechnet : "Fehler: Arbeitszeit wurde nicht abgerechnet!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (datum == null) {
            throw new IllegalStateException("Datum darf nicht fehlen!");
        }
        if (minuten <= 0) {
            throw new IllegalStateException("Minuten müssen größer als 0 sein!");
        }
    }

    /**
     * @return der Arbeitstag
     */
    public LocalDate showDatum() {
        return datum;
    }

    /**
     * @return die gearbeiteten Minuten
     */
    public int showMinuten() {
        return minuten;
    }

    /**
     * @return {@code true}, wenn die Arbeitszeit bereits abgerechnet ist
     */
    public boolean istAbgerechnet() {
        return abgerechnet;
    }
}
