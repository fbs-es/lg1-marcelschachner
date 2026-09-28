package fbs.lg1;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class Kunde {

    private final String kundenId;
    private final String name;
    private final String email;
    private final Clock clock;
    private final List<Ausleihung> ausleihungen = new ArrayList<>();
    private double guthaben;
    private boolean istKundeGesperrt;
    private boolean mahnungOffen;

    public Kunde(String kundenId, String name, String email, double guthaben, Clock clock) {
        if (kundenId == null || kundenId.isBlank()) {
            throw new IllegalArgumentException("Kunden-ID darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-Mail-Adresse ist ungültig!");
        }
        if (guthaben < 10.00) {
            throw new IllegalArgumentException("Startguthaben muss mindestens 10 € betragen!");
        }

        this.kundenId = kundenId;
        this.name = name;
        this.email = email;
        this.guthaben = guthaben;
        this.clock = clock;
        this.istKundeGesperrt = false;
        this.mahnungOffen = false;

        checkInvariant();
    }

    public boolean fahrtStarten(Scooter scooter) {
        if (scooter == null
                || findOffeneAusleihung() != null
                || !scooter.checkIstGesperrt()
                || scooter.checkAkkustand() <= 15
                || guthaben < 1.00
                || istKundeGesperrt) {
            return false;
        }

        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        ausleihungen.add(ausleihung);

        assert findOffeneAusleihung() == ausleihung : "Fehler: Ausleihe wurde nicht gespeichert!";
        assert ausleihung.showScooter() == scooter : "Fehler: Scooter wurde nicht zugewiesen!";
        assert !scooter.checkIstGesperrt() : "Fehler: Scooter wurde nicht entsperrt!";
        checkInvariant();
        return true;
    }

    public void fahrtBeenden() {
        Ausleihung ausleihung = findOffeneAusleihung();
        if (ausleihung == null) {
            throw new IllegalStateException("Kunde hat keinen Scooter ausgeliehen!");
        }

        double oldGuthaben = guthaben;

        ausleihung.beenden();
        guthaben = runden(guthaben - ausleihung.showKosten());

        if (guthaben < 0) {
            istKundeGesperrt = true;
            mahnungOffen = true;
            mahnungSenden();
        }

        assert guthaben == runden(oldGuthaben - ausleihung.showKosten())
                : "Fehler: Fahrtkosten wurden nicht korrekt abgezogen!";
        assert ausleihung.showScooter().checkIstGesperrt()
                : "Fehler: Scooter wurde nicht gesperrt!";
        assert guthaben >= 0 || (istKundeGesperrt && mahnungOffen)
                : "Fehler: Kunde wurde trotz negativem Guthaben nicht gesperrt!";
        checkInvariant();
    }

    public void kontoAusgleichen(double betrag) {
        if (betrag <= 0) {
            throw new IllegalArgumentException("Betrag muss positiv sein!");
        }

        double oldGuthaben = guthaben;

        guthaben = runden(guthaben + betrag);

        if (guthaben > 0) {
            istKundeGesperrt = false;
            mahnungOffen = false;
        }

        assert guthaben == runden(oldGuthaben + betrag)
                : "Fehler: Guthaben wurde nicht korrekt erhöht!";
        assert guthaben <= 0 || (!istKundeGesperrt && !mahnungOffen)
                : "Fehler: Sperre wurde trotz positivem Guthaben nicht aufgehoben!";
        checkInvariant();
    }

    private void mahnungSenden() {
        System.out.println(
                "Mahnung per E-Mail an "
                        + email
                        + ": Ihr Guthaben beträgt "
                        + guthaben
                        + " €. Ihr Konto ist gesperrt.");
    }

    private Ausleihung findOffeneAusleihung() {
        for (Ausleihung ausleihung : ausleihungen) {
            if (!ausleihung.istBeendet()) {
                return ausleihung;
            }
        }
        return null;
    }

    private double runden(double betrag) {
        return Math.round(betrag * 100) / 100.0;
    }

    private void checkInvariant() {
        if (kundenId == null || kundenId.isBlank()) {
            throw new IllegalStateException("Kunden-ID darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalStateException("E-Mail-Adresse ist ungültig!");
        }
        if (guthaben < 0 && !istKundeGesperrt) {
            throw new IllegalStateException("Kunde mit negativem Guthaben muss gesperrt sein!");
        }
        if (istKundeGesperrt != mahnungOffen) {
            throw new IllegalStateException("Sperre und Mahnung müssen übereinstimmen!");
        }
        int offeneFahrten = 0;
        for (Ausleihung ausleihung : ausleihungen) {
            if (!ausleihung.istBeendet()) {
                offeneFahrten++;
            }
        }
        if (offeneFahrten > 1) {
            throw new IllegalStateException(
                    "Kunde darf höchstens einen Scooter gleichzeitig ausleihen!");
        }
    }

    public String showKundenId() {
        return kundenId;
    }

    public String showName() {
        return name;
    }

    public String showEmail() {
        return email;
    }

    public double showGuthaben() {
        return guthaben;
    }

    public boolean istKundeGesperrt() {
        return istKundeGesperrt;
    }

    public boolean istMahnungOffen() {
        return mahnungOffen;
    }

    public List<Ausleihung> showAusleihungen() {
        return ausleihungen;
    }
}
