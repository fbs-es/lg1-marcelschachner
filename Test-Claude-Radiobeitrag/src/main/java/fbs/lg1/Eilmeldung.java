package fbs.lg1;

/**
 * Ein dringender {@link Radiobeitrag} mit einer Dringlichkeitsstufe von 1 bis 5 und der
 * betroffenen Region. Eilmeldungen können mit {@link Radiosendung#eilmeldungEinfuegen} spontan in
 * eine laufende Sendung eingeschoben werden.
 */
public class Eilmeldung extends Radiobeitrag {

    private final int dringlichkeitsstufe;
    private final String region;

    /**
     * Legt eine Eilmeldung an.
     *
     * @throws IllegalArgumentException wenn Titel oder Region leer sind, die Dauer nicht größer als
     *     0 ist oder die Dringlichkeitsstufe nicht zwischen 1 und 5 liegt
     */
    public Eilmeldung(String titel, int dauer, int dringlichkeitsstufe, String region) {
        super(titel, dauer);
        if (dringlichkeitsstufe < 1 || dringlichkeitsstufe > 5) {
            throw new IllegalArgumentException("Dringlichkeitsstufe muss zwischen 1 und 5 liegen!");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region darf nicht leer sein!");
        }

        this.dringlichkeitsstufe = dringlichkeitsstufe;
        this.region = region;

        checkInvariant();
    }

    /**
     * Erstellt eine Eilmeldung mit denselben Daten, dem Zusatz "(Fortsetzung)" im Titel und der
     * angegebenen Restdauer.
     */
    @Override
    protected Radiobeitrag erstelleFortsetzung(int restdauer) {
        String neuerTitel = showTitel() + " (Fortsetzung)";
        return new Eilmeldung(neuerTitel, restdauer, dringlichkeitsstufe, region);
    }

    private void checkInvariant() {
        if (dringlichkeitsstufe < 1 || dringlichkeitsstufe > 5) {
            throw new IllegalStateException("Dringlichkeitsstufe muss zwischen 1 und 5 liegen!");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("Region darf nicht leer sein!");
        }
    }

    /**
     * @return die Dringlichkeitsstufe von 1 bis 5
     */
    public int showDringlichkeitsstufe() {
        return dringlichkeitsstufe;
    }

    /**
     * @return die betroffene Region
     */
    public String showRegion() {
        return region;
    }
}
