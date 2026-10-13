package fbs.lg1;

public class Auszubildender extends Beschaeftigter {

    private final String ausbildungsberuf;
    private final int ausbildungsjahr;

    public Auszubildender(
            String vorname,
            String nachname,
            double gehalt,
            String ausbildungsberuf,
            int ausbildungsjahr) {
        super(vorname, nachname, gehalt);
        if (ausbildungsberuf == null || ausbildungsberuf.isBlank()) {
            throw new IllegalArgumentException("Ausbildungsberuf darf nicht leer sein!");
        }
        if (ausbildungsjahr < 1 || ausbildungsjahr > 4) {
            throw new IllegalArgumentException("Ausbildungsjahr muss zwischen 1 und 4 liegen!");
        }

        this.ausbildungsberuf = ausbildungsberuf;
        this.ausbildungsjahr = ausbildungsjahr;

        checkInvariant();
    }

    @Override
    public void gehaltAnpassen() {
        double oldGehalt = showGehalt();
        double erhoehung = 50.0 * ausbildungsjahr;
        double neuesGehalt = oldGehalt + erhoehung;

        aendereGehalt(neuesGehalt);

        assert showGehalt() > oldGehalt : "Fehler: Gehalt wurde nicht erhöht!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (ausbildungsberuf == null || ausbildungsberuf.isBlank()) {
            throw new IllegalStateException("Ausbildungsberuf darf nicht leer sein!");
        }
        if (ausbildungsjahr < 1 || ausbildungsjahr > 4) {
            throw new IllegalStateException("Ausbildungsjahr muss zwischen 1 und 4 liegen!");
        }
    }

    public String showAusbildungsberuf() {
        return ausbildungsberuf;
    }

    public int showAusbildungsjahr() {
        return ausbildungsjahr;
    }
}
