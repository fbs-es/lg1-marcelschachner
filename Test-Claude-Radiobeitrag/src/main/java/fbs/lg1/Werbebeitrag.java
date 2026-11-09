package fbs.lg1;

/**
 * Ein {@link Radiobeitrag} mit Werbung für einen Kunden und dessen verfügbarem Werbebudget.
 */
public class Werbebeitrag extends Radiobeitrag {

    private final String kunde;
    private final double werbebudget;

    /**
     * Legt einen Werbebeitrag an.
     *
     * @throws IllegalArgumentException wenn Titel oder Kunde leer sind, die Dauer nicht größer als
     *     0 ist oder das Werbebudget negativ ist
     */
    public Werbebeitrag(String titel, int dauer, String kunde, double werbebudget) {
        super(titel, dauer);
        if (kunde == null || kunde.isBlank()) {
            throw new IllegalArgumentException("Kunde darf nicht leer sein!");
        }
        if (werbebudget < 0) {
            throw new IllegalArgumentException("Werbebudget darf nicht negativ sein!");
        }

        this.kunde = kunde;
        this.werbebudget = werbebudget;

        checkInvariant();
    }

    /**
     * Erstellt einen Werbebeitrag mit denselben Daten, dem Zusatz "(Fortsetzung)" im Titel und der
     * angegebenen Restdauer.
     */
    @Override
    protected Radiobeitrag erstelleFortsetzung(int restdauer) {
        String neuerTitel = showTitel() + " (Fortsetzung)";
        return new Werbebeitrag(neuerTitel, restdauer, kunde, werbebudget);
    }

    private void checkInvariant() {
        if (kunde == null || kunde.isBlank()) {
            throw new IllegalStateException("Kunde darf nicht leer sein!");
        }
        if (werbebudget < 0) {
            throw new IllegalStateException("Werbebudget darf nicht negativ sein!");
        }
    }

    /**
     * @return der Name des Kunden
     */
    public String showKunde() {
        return kunde;
    }

    /**
     * @return das verfügbare Werbebudget in Euro
     */
    public double showWerbebudget() {
        return werbebudget;
    }
}
