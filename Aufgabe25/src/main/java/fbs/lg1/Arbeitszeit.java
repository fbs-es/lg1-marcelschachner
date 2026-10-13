package fbs.lg1;

import java.time.LocalDate;

public class Arbeitszeit {

    private final LocalDate datum;
    private final int minuten;
    private boolean abgerechnet;

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

    public LocalDate showDatum() {
        return datum;
    }

    public int showMinuten() {
        return minuten;
    }

    public boolean istAbgerechnet() {
        return abgerechnet;
    }
}
