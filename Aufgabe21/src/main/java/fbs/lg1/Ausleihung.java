package fbs.lg1;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

public class Ausleihung {

    private static int nextAusleiheNummer = 1;

    private final String ausleiheId;
    private final Scooter scooter;
    private final Clock clock;
    private final LocalDateTime startzeit;
    private LocalDateTime endzeit;
    private double kosten;

    public Ausleihung(Scooter scooter, Clock clock) {
        if (scooter == null) {
            throw new IllegalArgumentException("Scooter darf nicht fehlen!");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Uhr darf nicht fehlen!");
        }
        if (!scooter.checkIstGesperrt()) {
            throw new IllegalStateException("Scooter ist bereits in Benutzung!");
        }
        if (scooter.checkAkkustand() <= 15) {
            throw new IllegalStateException("Akkustand muss über 15% liegen!");
        }

        this.ausleiheId = String.format("A-%04d", nextAusleiheNummer++);
        this.scooter = scooter;
        this.clock = clock;
        this.startzeit = LocalDateTime.now(clock);
        this.kosten = 0.0;

        scooter.entsperren();

        assert !scooter.checkIstGesperrt() : "Fehler: Scooter wurde nicht entsperrt!";
        checkInvariant();
    }

    public void beenden() {
        if (istBeendet()) {
            throw new IllegalStateException("Ausleihe wurde bereits beendet!");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (!now.isAfter(startzeit)) {
            throw new IllegalStateException("Fahrtdauer muss größer als 0 Minuten sein!");
        }

        int oldAkkustand = scooter.checkAkkustand();

        this.endzeit = now;
        long minuten = berechneDauerInMinuten();
        this.kosten = minuten * 20 / 100.0;
        scooter.akkuVerringern((int) Math.min(minuten, 100));
        scooter.sperren();

        assert minuten > 0 : "Fehler: Fahrtdauer ist nicht positiv!";
        assert scooter.checkIstGesperrt() : "Fehler: Scooter wurde nicht gesperrt!";
        assert scooter.checkAkkustand() == Math.max(0, oldAkkustand - minuten)
                : "Fehler: Akkustand wurde nicht korrekt verringert!";
        checkInvariant();
    }

    private long berechneDauerInMinuten() {
        Duration dauer = Duration.between(startzeit, endzeit);
        long minuten = dauer.toMinutes();
        if (dauer.minusMinutes(minuten).isPositive()) {
            minuten++;
        }
        return minuten;
    }

    public boolean istBeendet() {
        return endzeit != null;
    }

    public String showAusleiheId() {
        return ausleiheId;
    }

    public Scooter showScooter() {
        return scooter;
    }

    public double showKosten() {
        return kosten;
    }

    public LocalDateTime showStartzeit() {
        return startzeit;
    }

    public LocalDateTime showEndzeit() {
        return endzeit;
    }

    private void checkInvariant() {
        if (startzeit == null) {
            throw new IllegalStateException("Startzeit darf nicht fehlen!");
        }
        if (kosten < 0) {
            throw new IllegalStateException("Kosten dürfen nicht negativ sein!");
        }
        if (endzeit != null && !endzeit.isAfter(startzeit)) {
            throw new IllegalStateException("Endzeit muss nach der Startzeit liegen!");
        }
    }
}
