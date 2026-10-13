package fbs.lg1;

public abstract class Beschaeftigter {

    private final String vorname;
    private final String nachname;
    private double gehalt;

    public Beschaeftigter(String vorname, String nachname, double gehalt) {
        if (vorname == null || vorname.isBlank()) {
            throw new IllegalArgumentException("Vorname darf nicht leer sein!");
        }
        if (nachname == null || nachname.isBlank()) {
            throw new IllegalArgumentException("Nachname darf nicht leer sein!");
        }
        if (gehalt < 0) {
            throw new IllegalArgumentException("Gehalt darf nicht negativ sein!");
        }

        this.vorname = vorname;
        this.nachname = nachname;
        this.gehalt = gehalt;

        checkInvariant();
    }

    public abstract void gehaltAnpassen();

    public void aendereGehalt(double neuesGehalt) {
        if (neuesGehalt < 0) {
            throw new IllegalArgumentException("Gehalt darf nicht negativ sein!");
        }

        gehalt = neuesGehalt;

        assert gehalt == neuesGehalt : "Fehler: Gehalt wurde nicht geändert!";
        checkInvariant();
    }

    private void checkInvariant() {
        if (vorname == null || vorname.isBlank()) {
            throw new IllegalStateException("Vorname darf nicht leer sein!");
        }
        if (nachname == null || nachname.isBlank()) {
            throw new IllegalStateException("Nachname darf nicht leer sein!");
        }
        if (gehalt < 0) {
            throw new IllegalStateException("Gehalt darf nicht negativ sein!");
        }
    }

    public String showVorname() {
        return vorname;
    }

    public String showNachname() {
        return nachname;
    }

    public double showGehalt() {
        return gehalt;
    }
}
