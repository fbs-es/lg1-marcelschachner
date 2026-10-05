package fbs.lg1;

/** Ein Lieferant, der Produkte anbietet. */
public class Lieferant {

    private final int lieferantennummer;
    private final String name;

    /**
     * Legt einen Lieferanten an.
     *
     * @throws IllegalArgumentException wenn Nummer oder Name ungültig sind
     */
    public Lieferant(int lieferantennummer, String name) {
        if (lieferantennummer <= 0) {
            throw new IllegalArgumentException("Lieferantennummer muss größer als 0 sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }

        this.lieferantennummer = lieferantennummer;
        this.name = name;

        checkInvariant();
    }

    private void checkInvariant() {
        if (lieferantennummer <= 0) {
            throw new IllegalStateException("Lieferantennummer muss größer als 0 sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
    }

    /** @return die Lieferantennummer */
    public int showLieferantennummer() {
        return lieferantennummer;
    }

    /** @return der Name */
    public String showName() {
        return name;
    }
}
