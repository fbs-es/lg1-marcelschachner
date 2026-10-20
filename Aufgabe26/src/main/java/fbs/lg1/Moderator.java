package fbs.lg1;

/**
 * Der Moderator einer {@link Radiosendung}. Erhält Warnungen, wenn die Sendung ihr Zeitlimit
 * überschreitet.
 */
public class Moderator {

    private final String name;

    /**
     * Legt einen Moderator an.
     *
     * @throws IllegalArgumentException wenn der Name leer ist
     */
    public Moderator(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }

        this.name = name;

        checkInvariant();
    }

    /**
     * Gibt eine Warnung für den Moderator auf der Konsole aus.
     *
     * @param meldung der Text der Warnung
     */
    public void warnungAnzeigen(String meldung) {
        System.out.println("WARNUNG an " + name + ": " + meldung);
    }

    private void checkInvariant() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
    }

    /**
     * @return der Name
     */
    public String showName() {
        return name;
    }
}
