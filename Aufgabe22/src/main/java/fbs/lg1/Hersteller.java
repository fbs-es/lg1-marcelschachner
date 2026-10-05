package fbs.lg1;

/**
 * Ein Hersteller, der viele {@link Produkt}e anbieten kann.
 *
 * <p>Die Zuordnung erfolgt über das Produkt: Jedes Produkt verweist auf genau einen Hersteller,
 * beliebig viele Produkte können auf denselben Hersteller verweisen.
 *
 * @invariant Herstellername ist nie {@code null} oder leer.
 * @invariant Die Support-E-Mail ist nie {@code null} und enthält ein {@code @}.
 * @see Produkt
 */
public class Hersteller {

    private final String name;
    private final String supportEmail;

    /**
     * Legt einen neuen Hersteller an.
     *
     * @param name der Herstellername
     * @param supportEmail die Support-E-Mail-Adresse
     * @pre {@code name} ist nicht {@code null} und nicht leer.
     * @pre {@code supportEmail} ist nicht {@code null} und enthält ein {@code @}.
     * @post {@link #showName()} liefert {@code name}, {@link #showSupportEmail()} liefert {@code
     *     supportEmail}.
     * @throws IllegalArgumentException wenn eine Vorbedingung verletzt ist
     */
    public Hersteller(String name, String supportEmail) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Herstellername darf nicht leer sein!");
        }
        if (supportEmail == null || !supportEmail.contains("@")) {
            throw new IllegalArgumentException("Support-E-Mail ist ungültig!");
        }

        this.name = name;
        this.supportEmail = supportEmail;

        checkInvariant();
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn eine Invariante verletzt ist
     */
    private void checkInvariant() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Herstellername darf nicht leer sein!");
        }
        if (supportEmail == null || !supportEmail.contains("@")) {
            throw new IllegalStateException("Support-E-Mail ist ungültig!");
        }
    }

    /**
     * Liefert den Herstellernamen.
     *
     * @return der Name, nie {@code null} oder leer
     */
    public String showName() {
        return name;
    }

    /**
     * Liefert die Support-E-Mail-Adresse.
     *
     * @return die E-Mail-Adresse, enthält immer ein {@code @}
     */
    public String showSupportEmail() {
        return supportEmail;
    }
}
