package fbs.lg1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Das Tagesprogramm eines {@link Radiosender}s mit allen {@link Radiosendung}en eines Datums.
 */
public class Radioprogramm {

    private final LocalDate datum;
    private final List<Radiosendung> sendungen = new ArrayList<>();

    /**
     * Legt ein leeres Programm für ein Datum an.
     *
     * @throws IllegalArgumentException wenn das Datum fehlt
     */
    public Radioprogramm(LocalDate datum) {
        if (datum == null) {
            throw new IllegalArgumentException("Datum darf nicht fehlen!");
        }

        this.datum = datum;

        checkInvariant();
    }

    /**
     * Nimmt eine Sendung in das Programm auf.
     *
     * @param sendung die neue Sendung
     * @throws IllegalArgumentException wenn die Sendung fehlt
     * @throws IllegalStateException wenn die Sendung bereits im Programm ist
     */
    public void sendungHinzufuegen(Radiosendung sendung) {
        if (sendung == null) {
            throw new IllegalArgumentException("Sendung darf nicht fehlen!");
        }
        if (sendungen.contains(sendung)) {
            throw new IllegalStateException("Sendung ist bereits im Programm!");
        }

        int oldAnzahl = sendungen.size();

        sendungen.add(sendung);

        assert sendungen.size() == oldAnzahl + 1 : "Fehler: Sendung wurde nicht hinzugefügt!";
        checkInvariant();
    }

    /**
     * Entfernt eine Sendung aus dem Programm.
     *
     * @param sendung die zu entfernende Sendung
     * @throws IllegalStateException wenn die Sendung nicht im Programm ist
     */
    public void sendungEntfernen(Radiosendung sendung) {
        if (!sendungen.contains(sendung)) {
            throw new IllegalStateException("Sendung ist nicht im Programm!");
        }

        int oldAnzahl = sendungen.size();

        sendungen.remove(sendung);

        assert sendungen.size() == oldAnzahl - 1 : "Fehler: Sendung wurde nicht entfernt!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (datum == null) {
            throw new IllegalStateException("Datum darf nicht fehlen!");
        }
        for (Radiosendung sendung : sendungen) {
            if (sendung == null) {
                throw new IllegalStateException("Sendung darf nicht fehlen!");
            }
        }
    }

    /**
     * @return das Datum des Programms
     */
    public LocalDate showDatum() {
        return datum;
    }

    /**
     * @return alle Sendungen, nicht veränderbar
     */
    public List<Radiosendung> showSendungen() {
        return Collections.unmodifiableList(sendungen);
    }
}
