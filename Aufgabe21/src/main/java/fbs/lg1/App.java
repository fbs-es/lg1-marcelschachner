package fbs.lg1;

import java.time.Duration;

/**
 * Demoprogramm für den Scooterverleih.
 *
 * <p>Spielt mit einer {@code FakeClock} einen kompletten Ablauf durch: normale Fahrt, Fahrt mit
 * zu leerem Akku, Beenden ohne laufende Fahrt, lange Fahrt mit Sperre und Mahnung sowie den
 * Kontoausgleich, und gibt zum Schluss die Historie aus. Alle Ausgaben erfolgen auf Deutsch.
 */
public class App {
    /**
     * Startet die Demo.
     *
     * @param args wird nicht verwendet
     */
    public static void main(String[] args) {
        FakeClock clock = FakeClock.start();
        Scooter scooter = new Scooter("S-001");
        Scooter leererScooter = new Scooter("S-002");
        leererScooter.akkuVerringern(90);
        Kunde kunde = new Kunde("K-001", "Marcel Schachner", "schachner.marcel@icloud.com", 10.00, clock);

        System.out.println("Kunde " + kunde.showName() + " mit Guthaben " + kunde.showGuthaben() + " €");

        kunde.fahrtStarten(scooter);
        System.out.println("Fahrt gestartet mit " + scooter.showScooterId());
        clock.advance(Duration.ofMinutes(10));
        kunde.fahrtBeenden();
        System.out.println("Fahrt beendet: Guthaben " + kunde.showGuthaben() + " €, Akku "
                + scooter.checkAkkustand() + "%");

        System.out.println("Fahrt mit " + leererScooter.showScooterId() + " (Akku "
                + leererScooter.checkAkkustand() + "%) möglich: " + kunde.fahrtStarten(leererScooter));

        try {
            kunde.fahrtBeenden();
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        kunde.fahrtStarten(scooter);
        clock.advance(Duration.ofMinutes(45));
        kunde.fahrtBeenden();
        System.out.println("Guthaben nach langer Fahrt: " + kunde.showGuthaben() + " €, gesperrt: "
                + kunde.istKundeGesperrt() + ", Mahnung offen: " + kunde.istMahnungOffen());
        System.out.println("Neue Fahrt möglich: " + kunde.fahrtStarten(scooter));

        kunde.kontoAusgleichen(5.00);
        System.out.println("Konto ausgeglichen: Guthaben " + kunde.showGuthaben() + " €, gesperrt: "
                + kunde.istKundeGesperrt() + ", Mahnung offen: " + kunde.istMahnungOffen());

        System.out.println("Historie:");
        for (Ausleihung ausleihung : kunde.showAusleihungen()) {
            System.out.println("  " + ausleihung.showAusleiheId() + ": " + ausleihung.showStartzeit()
                    + " bis " + ausleihung.showEndzeit() + ", Kosten " + ausleihung.showKosten() + " €");
        }
    }
}
