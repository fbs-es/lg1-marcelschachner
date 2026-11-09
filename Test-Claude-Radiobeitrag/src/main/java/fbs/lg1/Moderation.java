package fbs.lg1;

/**
 * Ein {@link Radiobeitrag}, in dem der Moderator einen vorbereiteten Sprechertext vorträgt.
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
     * Erstellt eine Moderation mit demselben Sprechertext, dem Zusatz "(Fortsetzung)" im Titel und
     * der angegebenen Restdauer.
     */
    @Override
    protected Radiobeitrag erstelleFortsetzung(int restdauer) {
        String neuerTitel = showTitel() + " (Fortsetzung)";
        return new Moderation(neuerTitel, restdauer, sprechertext);
    }

    private void checkInvariant() {
        if (sprechertext == null || sprechertext.isBlank()) {
            throw new IllegalStateException("Sprechertext darf nicht leer sein!");
        }
    }

    /**
     * @return der Sprechertext für den Moderator
     */
    public String showSprechertext() {
        return sprechertext;
    }
}
