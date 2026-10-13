package fbs.lg1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Aushilfe extends Beschaeftigter {

    private static final double STUNDENSATZ = 15.0;

    private final List<Arbeitszeit> arbeitszeiten = new ArrayList<>();

    public Aushilfe(String vorname, String nachname, double gehalt) {
        super(vorname, nachname, gehalt);

        checkInvariant();
    }

    public void arbeitszeitErfassen(LocalDate datum, int minuten) {
        int oldAnzahl = arbeitszeiten.size();

        Arbeitszeit arbeitszeit = new Arbeitszeit(datum, minuten);
        arbeitszeiten.add(arbeitszeit);

        assert arbeitszeiten.size() == oldAnzahl + 1 : "Fehler: Arbeitszeit wurde nicht gespeichert!";
        checkInvariant();
    }

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

    public List<Arbeitszeit> showArbeitszeiten() {
        return arbeitszeiten;
    }
}
