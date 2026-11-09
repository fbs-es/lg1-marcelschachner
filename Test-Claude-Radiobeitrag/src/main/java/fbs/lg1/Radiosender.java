package fbs.lg1;

/**
 * Ein Radiosender mit Namen, Übertragungsfrequenz und seinem {@link Radioprogramm}.
 */
public class Radiosender {

    private final String name;
    private final double frequenz;
    private final Radioprogramm radioprogramm;

    /**
     * Legt einen Radiosender an.
     *
     * @param name der Name des Senders
     * @param frequenz die Übertragungsfrequenz in MHz
     * @param radioprogramm das Programm des Senders
     * @throws IllegalArgumentException wenn der Name leer ist, die Frequenz nicht größer als 0 ist
     *     oder das Programm fehlt
     */
    public Radiosender(String name, double frequenz, Radioprogramm radioprogramm) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (frequenz <= 0) {
            throw new IllegalArgumentException("Frequenz muss größer als 0 sein!");
        }
        if (radioprogramm == null) {
            throw new IllegalArgumentException("Radioprogramm darf nicht fehlen!");
        }

        this.name = name;
        this.frequenz = frequenz;
        this.radioprogramm = radioprogramm;

        checkInvariant();
    }

    private void checkInvariant() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (frequenz <= 0) {
            throw new IllegalStateException("Frequenz muss größer als 0 sein!");
        }
        if (radioprogramm == null) {
            throw new IllegalStateException("Radioprogramm darf nicht fehlen!");
        }
    }

    /**
     * @return der Name des Senders
     */
    public String showName() {
        return name;
    }

    /**
     * @return die Übertragungsfrequenz in MHz
     */
    public double showFrequenz() {
        return frequenz;
    }

    /**
     * @return das Radioprogramm des Senders
     */
    public Radioprogramm showRadioprogramm() {
        return radioprogramm;
    }
}
