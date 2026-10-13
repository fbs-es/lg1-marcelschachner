package fbs.lg1;

/**
 * Ein fest angestellter {@link Beschaeftigter} mit Einstellungsjahr und Urlaubsanspruch.
 *
 * <p>Bei jeder Gehaltsanpassung steigt das Gehalt um 3 %.
 */
public class Angestellter extends Beschaeftigter {

    private final int einstellungsjahr;
    private final int urlaubsanspruch;

    /**
     * Legt einen Angestellten an.
     *
     * @throws IllegalArgumentException wenn ein Name leer, das Gehalt oder der Urlaubsanspruch
     *     negativ oder das Einstellungsjahr nicht größer als 0 ist
     */
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

    /**
     * Erhöht das Gehalt um 3 %.
     */
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

    /**
     * @return das Jahr der Einstellung
     */
    public int showEinstellungsjahr() {
        return einstellungsjahr;
    }

    /**
     * @return der Urlaubsanspruch in Tagen
     */
    public int showUrlaubsanspruch() {
        return urlaubsanspruch;
    }
}
