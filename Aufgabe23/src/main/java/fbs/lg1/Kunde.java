package fbs.lg1;

/** Ein Kunde des Online-Shops. */
public class Kunde {

    private final int kundennummer;
    private final String name;
    private final String email;

    /**
     * Legt einen Kunden an.
     *
     * @throws IllegalArgumentException wenn Nummer, Name oder E-Mail ungültig sind
     */
    public Kunde(int kundennummer, String name, String email) {
        if (kundennummer <= 0) {
            throw new IllegalArgumentException("Kundennummer muss größer als 0 sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-Mail-Adresse ist ungültig!");
        }

        this.kundennummer = kundennummer;
        this.name = name;
        this.email = email;

        checkInvariant();
    }

    private void checkInvariant() {
        if (kundennummer <= 0) {
            throw new IllegalStateException("Kundennummer muss größer als 0 sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalStateException("E-Mail-Adresse ist ungültig!");
        }
    }

    /** @return die Kundennummer */
    public int showKundennummer() {
        return kundennummer;
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
