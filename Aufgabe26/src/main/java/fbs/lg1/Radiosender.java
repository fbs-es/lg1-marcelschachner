package fbs.lg1;

/**
 * Ein Radiosender mit Name, Frequenz und seinem {@link Radioprogramm}.
 */
public class Radiosender {

    private final String name;
    private final double frequenz;
    private final Radioprogramm programm;

    /**
     * Legt einen Radiosender an.
     *
     * @throws IllegalArgumentException wenn der Name leer ist, die Frequenz nicht größer als 0 ist
     *     oder das Programm fehlt
     */
    public Radiosender(String name, double frequenz, Radioprogramm programm) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (frequenz <= 0) {
            throw new IllegalArgumentException("Frequenz muss größer als 0 sein!");
        }
        if (programm == null) {
            throw new IllegalArgumentException("Programm darf nicht fehlen!");
        }

        this.name = name;
        this.frequenz = frequenz;
        this.programm = programm;

        checkInvariant();
    }

    private void checkInvariant() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (frequenz <= 0) {
            throw new IllegalStateException("Frequenz muss größer als 0 sein!");
        }
        if (programm == null) {
            throw new IllegalStateException("Programm darf nicht fehlen!");
        }
    }

    /**
     * @return der Name des Senders
     */
    public String showName() {
        return name;
    }

    /**
     * @return die Frequenz in MHz
     */
    public double showFrequenz() {
        return frequenz;
    }

    /**
     * @return das Programm des Senders
     */
    public Radioprogramm showProgramm() {
        return programm;
    }
}
