package fbs.lg1;

/**
 * Ein Leih-Scooter mit Akkustand und Sperrzustand.
 *
 * <p>Ein neuer Scooter hat einen Akkustand von 100 % und ist gesperrt. Ausgeliehen wird er über
 * {@link Ausleihung}, die ihn entsperrt, den Akku verringert und ihn am Ende wieder sperrt.
 *
 * <p><b>Invarianten:</b>
 * <ul>
 *   <li>Der Akkustand liegt immer zwischen 0 und 100.</li>
 *   <li>Die Scooter-ID ist nie {@code null} oder leer.</li>
 * </ul>
 *
 * <p>Diese Klasse ist nicht thread-sicher.
 *
 * @see Ausleihung
 */
public class Scooter {

    private final String scooterId;
    private int akkustand;
    private boolean istGesperrt;

    /**
     * Erzeugt einen gesperrten Scooter mit vollem Akku (100 %).
     *
     * @param scooterId die eindeutige ID des Scooters, nicht {@code null} und nicht leer
     * @throws IllegalArgumentException wenn {@code scooterId} {@code null} oder leer ist
     */
    public Scooter(String scooterId) {
        if (scooterId == null || scooterId.isBlank()) {
            throw new IllegalArgumentException("Scooter-ID darf nicht leer sein!");
        }

        this.scooterId = scooterId;
        this.akkustand = 100;
        this.istGesperrt = true;

        checkInvariant();
    }

    /**
     * Liefert die ID des Scooters.
     *
     * @return die Scooter-ID, nie {@code null} oder leer
     */
    public String showScooterId() {
        return scooterId;
    }

    /**
     * Liefert den aktuellen Akkustand.
     *
     * @return der Akkustand in Prozent, zwischen 0 und 100
     */
    public int checkAkkustand() {
        return akkustand;
    }

    /**
     * Prüft, ob der Scooter gesperrt ist.
     *
     * @return {@code true}, wenn der Scooter gesperrt (also frei) ist, {@code false}, wenn er
     *         entsperrt (also in Benutzung) ist
     */
    public boolean checkIstGesperrt() {
        return istGesperrt;
    }

    /**
     * Entsperrt den Scooter.
     *
     * @throws IllegalStateException wenn der Scooter bereits entsperrt ist
     */
    public void entsperren() {
        checkInvariant();
        if (!istGesperrt) {
            throw new IllegalStateException("Scooter ist bereits entsperrt!");
        }

        istGesperrt = false;

        assert !istGesperrt : "Fehler: Scooter wurde nicht entsperrt!";
        checkInvariant();
    }

    /**
     * Sperrt den Scooter.
     *
     * @throws IllegalStateException wenn der Scooter bereits gesperrt ist
     */
    public void sperren() {
        checkInvariant();
        if (istGesperrt) {
            throw new IllegalStateException("Scooter ist bereits gesperrt!");
        }

        istGesperrt = true;

        assert istGesperrt : "Fehler: Scooter wurde nicht gesperrt!";
        checkInvariant();
    }

    /**
     * Verringert den Akkustand. Der Akkustand sinkt dabei nie unter 0.
     *
     * @param prozent die Anzahl Prozentpunkte, um die der Akku sinkt, größer als 0
     * @throws IllegalArgumentException wenn {@code prozent} 0 oder negativ ist
     */
    public void akkuVerringern(int prozent) {
        checkInvariant();
        if (prozent <= 0) {
            throw new IllegalArgumentException("Prozentwert muss positiv sein!");
        }

        int oldAkkustand = akkustand;

        akkustand = Math.max(0, akkustand - prozent);

        assert akkustand == Math.max(0, oldAkkustand - prozent)
                : "Fehler: Akkustand wurde nicht korrekt verringert!";
        checkInvariant();
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn der Akkustand außerhalb von 0 bis 100 liegt oder die
     *                               Scooter-ID leer ist
     */
    private void checkInvariant() {
        if (akkustand < 0 || akkustand > 100) {
            throw new IllegalStateException("Akkustand muss zwischen 0 und 100 liegen!");
        }
        if (scooterId == null || scooterId.isBlank()) {
            throw new IllegalStateException("Scooter-ID darf nicht leer sein!");
        }
    }
}
