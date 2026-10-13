package fbs.lg1;

public class Angestellter extends Beschaeftigter {

    private final int einstellungsjahr;
    private final int urlaubsanspruch;

    public Angestellter(
            String vorname, String nachname, double gehalt, int einstellungsjahr, int urlaubsanspruch) {
        super(vorname, nachname, gehalt);
        if (einstellungsjahr <= 0) {
            throw new IllegalArgumentException("Einstellungsjahr muss größer als 0 sein!");
        }
        if (urlaubsanspruch < 0) {
            throw new IllegalArgumentException("Urlaubsanspruch darf nicht negativ sein!");
        }

        this.einstellungsjahr = einstellungsjahr;
        this.urlaubsanspruch = urlaubsanspruch;

        checkInvariant();
    }

    @Override
    public void gehaltAnpassen() {
        double oldGehalt = showGehalt();
        double neuesGehalt = oldGehalt * 1.03;

        aendereGehalt(neuesGehalt);

        assert showGehalt() >= oldGehalt : "Fehler: Gehalt wurde verringert!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (einstellungsjahr <= 0) {
            throw new IllegalStateException("Einstellungsjahr muss größer als 0 sein!");
        }
        if (urlaubsanspruch < 0) {
            throw new IllegalStateException("Urlaubsanspruch darf nicht negativ sein!");
        }
    }

    public int showEinstellungsjahr() {
        return einstellungsjahr;
    }

    public int showUrlaubsanspruch() {
        return urlaubsanspruch;
    }
}
