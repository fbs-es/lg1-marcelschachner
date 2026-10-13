package fbs.lg1;

/**
 * Ein {@link Beschaeftigter} in Ausbildung mit Ausbildungsberuf und Ausbildungsjahr (1 bis 4).
 *
 * <p>Bei jeder Gehaltsanpassung steigt das Gehalt um 50 € pro Ausbildungsjahr.
 */
public class Auszubildender extends Beschaeftigter {

    private final String ausbildungsberuf;
    private final int ausbildungsjahr;

    /**
     * Legt einen Auszubildenden an.
     *
     * @throws IllegalArgumentException wenn ein Name oder der Ausbildungsberuf leer, das Gehalt
     *     negativ oder das Ausbildungsjahr nicht zwischen 1 und 4 ist
     */
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

    /**
     * Erhöht das Gehalt um 50 € mal Ausbildungsjahr.
     */
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

    /**
     * @return der Ausbildungsberuf
     */
    public String showAusbildungsberuf() {
        return ausbildungsberuf;
    }

    /**
     * @return das aktuelle Ausbildungsjahr
     */
    public int showAusbildungsjahr() {
        return ausbildungsjahr;
    }
}
