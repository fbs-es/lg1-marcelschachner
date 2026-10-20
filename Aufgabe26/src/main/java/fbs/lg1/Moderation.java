package fbs.lg1;

/**
 * Ein gesprochener {@link Radiobeitrag} des Moderators mit Sprechertext.
 */
public class Moderation extends Radiobeitrag {

    private final String sprechertext;

    /**
     * Legt eine Moderation an.
     *
     * @throws IllegalArgumentException wenn Titel oder Sprechertext leer sind oder die Dauer nicht
     *     größer als 0 ist
     */
    public Moderation(String titel, int dauer, String sprechertext) {
        super(titel, dauer);
        if (sprechertext == null || sprechertext.isBlank()) {
            throw new IllegalArgumentException("Sprechertext darf nicht leer sein!");
        }

        this.sprechertext = sprechertext;

        checkInvariant();
    }

    /**
     * Erzeugt eine Moderation mit gleichem Titel und Sprechertext.
     *
     * @param dauer die Dauer der Kopie in Sekunden
     * @return die neue Moderation
     */
    @Override
    public Radiobeitrag kopieMitDauer(int dauer) {
        String titel = showTitel();
        Moderation kopie = new Moderation(titel, dauer, sprechertext);
        return kopie;
    }

    private void checkInvariant() {
        if (sprechertext == null || sprechertext.isBlank()) {
            throw new IllegalStateException("Sprechertext darf nicht leer sein!");
        }
    }
}
