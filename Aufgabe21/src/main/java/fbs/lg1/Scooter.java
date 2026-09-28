package fbs.lg1;

public class Scooter {

    private final String scooterId;
    private int akkustand;
    private boolean istGesperrt;

    public Scooter(String scooterId) {
        if (scooterId == null || scooterId.isBlank()) {
            throw new IllegalArgumentException("Scooter-ID darf nicht leer sein!");
        }

        this.scooterId = scooterId;
        this.akkustand = 100;
        this.istGesperrt = true;

        checkInvariant();
    }

    public String showScooterId() {
        return scooterId;
    }

    public int checkAkkustand() {
        return akkustand;
    }

    public boolean checkIstGesperrt() {
        return istGesperrt;
    }

    public void entsperren() {
        checkInvariant();
        if (!istGesperrt) {
            throw new IllegalStateException("Scooter ist bereits entsperrt!");
        }

        istGesperrt = false;

        assert !istGesperrt : "Fehler: Scooter wurde nicht entsperrt!";
        checkInvariant();
    }

    public void sperren() {
        checkInvariant();
        if (istGesperrt) {
            throw new IllegalStateException("Scooter ist bereits gesperrt!");
        }

        istGesperrt = true;

        assert istGesperrt : "Fehler: Scooter wurde nicht gesperrt!";
        checkInvariant();
    }

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

    private void checkInvariant() {
        if (akkustand < 0 || akkustand > 100) {
            throw new IllegalStateException("Akkustand muss zwischen 0 und 100 liegen!");
        }
        if (scooterId == null || scooterId.isBlank()) {
            throw new IllegalStateException("Scooter-ID darf nicht leer sein!");
        }
    }
}
