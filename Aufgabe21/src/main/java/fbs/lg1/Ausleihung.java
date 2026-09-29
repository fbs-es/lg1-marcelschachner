package fbs.lg1;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Eine einzelne Ausleihe eines {@link Scooter}s.
 *
 * <p>Der Lebenszyklus besteht aus zwei Zuständen:
 * <ol>
 *   <li><b>Laufend:</b> Nach dem Erzeugen ist der Scooter entsperrt und die Startzeit gesetzt.
 *       {@link #showEndzeit()} liefert {@code null}, {@link #showKosten()} liefert {@code 0.0}.</li>
 *   <li><b>Beendet:</b> Nach {@link #beenden()} sind Endzeit und Kosten gesetzt, der Scooter ist
 *       wieder gesperrt und sein Akku wurde verringert. Eine beendete Ausleihe ist endgültig.</li>
 * </ol>
 *
 * <p><b>Tarif:</b> Jede angefangene Minute kostet 0,20 &euro;. Beispiel: 61 Sekunden ergeben
 * 2 Minuten und damit 0,40 &euro;.
 *
 * <p><b>Akkuverbrauch:</b> Pro angefangener Minute sinkt der Akkustand um 1 Prozentpunkt,
 * höchstens jedoch um 100. Der Akkustand des Scooters sinkt nie unter 0.
 *
 * <p><b>Zeit:</b> Alle Zeitpunkte stammen aus der beim Erzeugen übergebenen {@link Clock}.
 * Dadurch lässt sich die Zeit in Tests kontrollieren.
 *
 * <p><b>Invarianten:</b>
 * <ul>
 *   <li>Die Startzeit ist immer gesetzt.</li>
 *   <li>Die Kosten sind nie negativ.</li>
 *   <li>Falls eine Endzeit gesetzt ist, liegt sie nach der Startzeit.</li>
 * </ul>
 *
 * <p>Diese Klasse ist nicht thread-sicher.
 *
 * <p>Beispiel:
 * <pre>{@code
 * Clock clock = Clock.systemDefaultZone();
 * Ausleihung ausleihung = new Ausleihung(scooter, clock);
 * // ... Fahrt ...
 * ausleihung.beenden();
 * double kosten = ausleihung.showKosten();
 * }</pre>
 *
 * @see Scooter
 * @see Kunde
 */
public class Ausleihung {

    /** Zähler für die nächste Ausleihe-ID. Gilt für alle Instanzen und wird nie zurückgesetzt. */
    private static int nextAusleiheNummer = 1;

    private final String ausleiheId;
    private final Scooter scooter;
    private final Clock clock;
    private final LocalDateTime startzeit;
    private LocalDateTime endzeit;
    private double kosten;

    /**
     * Startet eine neue Ausleihe.
     *
     * <p>Vergibt eine fortlaufende ID im Format {@code A-0001}, speichert die aktuelle Zeit
     * der {@code clock} als Startzeit und entsperrt den Scooter.
     *
     * @param scooter der auszuleihende Scooter, nicht {@code null}; muss gesperrt sein und
     *                einen Akkustand über 15 % haben
     * @param clock   die Zeitquelle für Start- und Endzeit, nicht {@code null}
     * @throws IllegalArgumentException wenn {@code scooter} oder {@code clock} {@code null} ist
     * @throws IllegalStateException    wenn der Scooter nicht gesperrt (bereits in Benutzung)
     *                                  ist oder sein Akkustand 15 % oder weniger beträgt
     */
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

    /**
     * Beendet die Ausleihe.
     *
     * <p>Setzt die Endzeit auf die aktuelle Zeit der {@code clock}, berechnet die Kosten
     * (0,20 &euro; je angefangener Minute), verringert den Akku des Scooters um die Anzahl der
     * angefangenen Minuten (höchstens 100) und sperrt den Scooter wieder.
     *
     * @throws IllegalStateException wenn die Ausleihe bereits beendet wurde oder seit dem Start
     *                               keine Zeit vergangen ist (Fahrtdauer 0)
     */
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

    /**
     * Berechnet die Fahrtdauer und rundet angefangene Minuten auf.
     *
     * @return die Dauer zwischen Start und Ende in vollen Minuten, aufgerundet
     */
    private long berechneDauerInMinuten() {
        Duration dauer = Duration.between(startzeit, endzeit);
        long minuten = dauer.toMinutes();
        if (dauer.minusMinutes(minuten).isPositive()) {
            minuten++;
        }
        return minuten;
    }

    /**
     * Prüft, ob die Ausleihe beendet wurde.
     *
     * @return {@code true}, wenn {@link #beenden()} erfolgreich aufgerufen wurde
     */
    public boolean istBeendet() {
        return endzeit != null;
    }

    /**
     * Liefert die eindeutige ID der Ausleihe.
     *
     * @return die ID im Format {@code A-0001}, {@code A-0002}, ...
     */
    public String showAusleiheId() {
        return ausleiheId;
    }

    /**
     * Liefert den ausgeliehenen Scooter.
     *
     * @return der Scooter dieser Ausleihe, nie {@code null}
     */
    public Scooter showScooter() {
        return scooter;
    }

    /**
     * Liefert die Kosten der Ausleihe.
     *
     * @return die Kosten in Euro; {@code 0.0}, solange die Ausleihe läuft
     */
    public double showKosten() {
        return kosten;
    }

    /**
     * Liefert den Startzeitpunkt.
     *
     * @return die Startzeit, nie {@code null}
     */
    public LocalDateTime showStartzeit() {
        return startzeit;
    }

    /**
     * Liefert den Endzeitpunkt.
     *
     * @return die Endzeit oder {@code null}, solange die Ausleihe läuft
     */
    public LocalDateTime showEndzeit() {
        return endzeit;
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn die Startzeit fehlt, die Kosten negativ sind oder die
     *                               Endzeit nicht nach der Startzeit liegt
     */
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
