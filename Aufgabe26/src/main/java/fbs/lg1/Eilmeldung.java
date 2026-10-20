package fbs.lg1;

/**
 * Eine dringende Meldung als {@link Radiobeitrag} für eine Region.
 *
 * <p>Die Dringlichkeit liegt zwischen 1 und 5. Eine Eilmeldung kann mit
 * {@link Radiosendung#eilmeldungEinfuegen(Eilmeldung, int)} mitten in einen laufenden Beitrag
 * eingefügt werden.
 */
public class Eilmeldung extends Radiobeitrag {

    private final String region;
    private int dringlichkeit;

    /**
     * Legt eine Eilmeldung an.
     *
     * @throws IllegalArgumentException wenn Titel oder Region leer sind, die Dauer nicht größer als 0
     *     oder die Dringlichkeit nicht zwischen 1 und 5 ist
     */
    public Eilmeldung(String titel, int dauer, int dringlichkeit, String region) {
        super(titel, dauer);
        if (dringlichkeit < 1 || dringlichkeit > 5) {
            throw new IllegalArgumentException("Dringlichkeit muss zwischen 1 und 5 liegen!");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region darf nicht leer sein!");
        }

        this.region = region;
        this.dringlichkeit = dringlichkeit;

        checkInvariant();
    }

    /**
     * Setzt die Dringlichkeit auf eine neue Stufe.
     *
     * @param stufe die neue Dringlichkeit zwischen 1 und 5
     * @throws IllegalArgumentException wenn die Stufe nicht zwischen 1 und 5 liegt
     */
    public void aendereDringlichkeit(int stufe) {
        if (stufe < 1 || stufe > 5) {
            throw new IllegalArgumentException("Dringlichkeit muss zwischen 1 und 5 liegen!");
        }

        dringlichkeit = stufe;

        assert dringlichkeit == stufe : "Fehler: Dringlichkeit wurde nicht geändert!";
        checkInvariant();
    }

    /**
     * Erzeugt eine Eilmeldung mit gleichem Titel, gleicher Dringlichkeit und Region.
     *
     * @param dauer die Dauer der Kopie in Sekunden
     * @return die neue Eilmeldung
     */
    @Override
    public Radiobeitrag kopieMitDauer(int dauer) {
        String titel = showTitel();
        Eilmeldung kopie = new Eilmeldung(titel, dauer, dringlichkeit, region);
        return kopie;
    }

    private void checkInvariant() {
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("Region darf nicht leer sein!");
        }
        if (dringlichkeit < 1 || dringlichkeit > 5) {
            throw new IllegalStateException("Dringlichkeit muss zwischen 1 und 5 liegen!");
        }
    }
}
