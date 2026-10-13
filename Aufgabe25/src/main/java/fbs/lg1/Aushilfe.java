package fbs.lg1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Eine Aushilfe, die nach erfassten {@link Arbeitszeit}en bezahlt wird.
 *
 * <p>Bei jeder Gehaltsanpassung werden alle noch nicht abgerechneten Arbeitszeiten mit 15 € pro
 * Stunde zum Gehalt addiert und als abgerechnet markiert.
 */
public class Aushilfe extends Beschaeftigter {

    private static final double STUNDENSATZ = 15.0;

    private final List<Arbeitszeit> arbeitszeiten = new ArrayList<>();

    /**
     * Legt eine Aushilfe ohne erfasste Arbeitszeiten an.
     *
     * @throws IllegalArgumentException wenn ein Name leer oder das Gehalt negativ ist
     */
    public Aushilfe(String vorname, String nachname, double gehalt) {
        super(vorname, nachname, gehalt);

        checkInvariant();
    }

    /**
     * Erfasst eine neue, noch nicht abgerechnete Arbeitszeit.
     *
     * @param datum der Arbeitstag
     * @param minuten die gearbeiteten Minuten
     * @throws IllegalArgumentException wenn das Datum fehlt oder die Minuten nicht größer als 0 sind
     */
    public void arbeitszeitErfassen(LocalDate datum, int minuten) {
        int oldAnzahl = arbeitszeiten.size();

        Arbeitszeit arbeitszeit = new Arbeitszeit(datum, minuten);
        arbeitszeiten.add(arbeitszeit);

        assert arbeitszeiten.size() == oldAnzahl + 1 : "Fehler: Arbeitszeit wurde nicht gespeichert!";
        checkInvariant();
    }

    /**
     * Rechnet alle offenen Arbeitszeiten mit 15 € pro Stunde ab und addiert den Betrag zum Gehalt.
     */
    @Override
    public void gehaltAnpassen() {
        double oldGehalt = showGehalt();

        int minuten = 0;
        for (Arbeitszeit arbeitszeit : arbeitszeiten) {
            if (!arbeitszeit.istAbgerechnet()) {
                minuten += arbeitszeit.showMinuten();
                arbeitszeit.abrechnen();
            }
        }
        double stunden = minuten / 60.0;
        double neuesGehalt = oldGehalt + stunden * STUNDENSATZ;

        aendereGehalt(neuesGehalt);

        assert showGehalt() >= oldGehalt : "Fehler: Gehalt wurde verringert!";
        checkInvariant();
    }

    private void checkInvariant() {
        for (Arbeitszeit arbeitszeit : arbeitszeiten) {
            if (arbeitszeit == null) {
                throw new IllegalStateException("Arbeitszeit darf nicht fehlen!");
            }
        }
    }

    /**
     * @return alle erfassten Arbeitszeiten, nicht veränderbar
     */
    public List<Arbeitszeit> showArbeitszeiten() {
        return Collections.unmodifiableList(arbeitszeiten);
    }
}
