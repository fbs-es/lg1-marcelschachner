package fbs.lg1;

/** Ein Nutzer, der in einer {@link Bibliothek} registriert werden kann. */
public class Nutzer {

    private final int id;
    private final String name;
    private final String email;

    /**
     * Legt einen Nutzer an.
     *
     * @throws IllegalArgumentException wenn ID, Name oder E-Mail ungültig sind
     */
    public Nutzer(int id, String name, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID muss größer als 0 sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-Mail-Adresse ist ungültig!");
        }

        this.id = id;
        this.name = name;
        this.email = email;

        checkInvariant();
    }

    private void checkInvariant() {
        if (id <= 0) {
            throw new IllegalStateException("ID muss größer als 0 sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalStateException("E-Mail-Adresse ist ungültig!");
        }
    }

    /** @return die eindeutige ID */
    public int showId() {
        return id;
    }

    /** @return der Name */
    public String showName() {
        return name;
    }

    /** @return die E-Mail-Adresse */
    public String showEmail() {
        return email;
    }
}
