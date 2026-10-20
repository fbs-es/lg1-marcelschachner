package fbs.lg1;

/**
 * Ein Werbespot als {@link Radiobeitrag} mit Kunde und Werbebudget.
 */
public class Werbebeitrag extends Radiobeitrag {

    private final String kunde;
    private final double werbebudget;

    /**
     * Legt einen Werbebeitrag an.
     *
     * @throws IllegalArgumentException wenn Titel oder Kunde leer sind, die Dauer nicht größer als 0
     *     oder das Werbebudget negativ ist
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
     * Erzeugt einen Werbebeitrag mit gleichem Titel, Kunden und Werbebudget.
     *
     * @param dauer die Dauer der Kopie in Sekunden
     * @return der neue Werbebeitrag
     */
    @Override
    public Radiobeitrag kopieMitDauer(int dauer) {
        String titel = showTitel();
        Werbebeitrag kopie = new Werbebeitrag(titel, dauer, kunde, werbebudget);
        return kopie;
    }

    private void checkInvariant() {
        if (kunde == null || kunde.isBlank()) {
            throw new IllegalStateException("Kunde darf nicht leer sein!");
        }
        if (werbebudget < 0) {
            throw new IllegalStateException("Werbebudget darf nicht negativ sein!");
        }
    }
}
