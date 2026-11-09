package fbs.lg1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Das Radioprogramm eines Tages. Es verwaltet die zeitliche Abfolge der {@link Radiosendung}en,
 * sortiert nach Startzeit und ohne überlappende Zeitblöcke.
 */
public class Radioprogramm {

    private final LocalDate datum;
    private final List<Radiosendung> sendungen = new ArrayList<>();

    /**
     * Legt ein leeres Radioprogramm für einen Tag an.
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
     * Nimmt eine Sendung an der passenden Stelle nach Startzeit in das Programm auf.
     *
     * @throws IllegalArgumentException wenn die Sendung fehlt
     * @throws IllegalStateException wenn die Sendung bereits im Programm ist oder sich ihr
     *     Zeitblock mit einer anderen Sendung überschneidet
     */
    public void sendungHinzufuegen(Radiosendung sendung) {
        if (sendung == null) {
            throw new IllegalArgumentException("Sendung darf nicht fehlen!");
        }
        if (sendungen.contains(sendung)) {
            throw new IllegalStateException("Sendung ist bereits im Programm!");
        }
        if (findUeberschneidung(sendung) != null) {
            throw new IllegalStateException("Zeitblock überschneidet sich mit einer anderen Sendung!");
        }

        int oldAnzahl = sendungen.size();

        int index = 0;
        for (Radiosendung eintrag : sendungen) {
            if (eintrag.showStartzeit().isBefore(sendung.showStartzeit())) {
                index++;
            }
        }
        sendungen.add(index, sendung);

        assert sendungen.size() == oldAnzahl + 1 : "Fehler: Sendung wurde nicht hinzugefügt!";
        checkInvariant();
    }

    private Radiosendung findUeberschneidung(Radiosendung sendung) {
        for (Radiosendung eintrag : sendungen) {
            boolean beginntVorEnde = sendung.showStartzeit().isBefore(eintrag.showEndzeit());
            boolean endetNachBeginn = sendung.showEndzeit().isAfter(eintrag.showStartzeit());
            if (beginntVorEnde && endetNachBeginn) {
                return eintrag;
            }
        }
        return null;
    }

    private void checkInvariant() {
        if (datum == null) {
            throw new IllegalStateException("Datum darf nicht fehlen!");
        }
        Radiosendung vorherige = null;
        for (Radiosendung sendung : sendungen) {
            if (sendung == null) {
                throw new IllegalStateException("Programm enthält eine fehlende Sendung!");
            }
            if (vorherige != null && sendung.showStartzeit().isBefore(vorherige.showEndzeit())) {
                throw new IllegalStateException("Sendungen sind nicht in zeitlicher Abfolge!");
            }
            vorherige = sendung;
        }
    }

    /**
     * @return der Tag des Programms
     */
    public LocalDate showDatum() {
        return datum;
    }

    /**
     * @return alle Sendungen nach Startzeit sortiert, nicht verändern
     */
    public List<Radiosendung> showSendungen() {
        return sendungen;
    }
}
