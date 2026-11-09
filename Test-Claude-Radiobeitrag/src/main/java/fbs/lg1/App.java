package fbs.lg1;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Demoprogramm für die Sendeplan-Software „Test-Claude-Radiobeitrag“.
 *
 * <p>Legt den Sender Cloud-FM mit einem Tagesprogramm und zwei Sendungen an, plant Beiträge ein,
 * zeigt eine abgelehnte Planung, kürzt, löscht und verschiebt Beiträge und schiebt zum Schluss eine
 * Eilmeldung mitten in einen laufenden Beitrag, wodurch das Zeitlimit überschritten wird. Alle
 * Ausgaben erfolgen auf Deutsch.
 */
public class App {

    /**
     * Startet die Demo.
     *
     * @param args wird nicht verwendet
     */
    public static void main(String[] args) {
        Radioprogramm programm = new Radioprogramm(LocalDate.of(2026, 11, 9));
        Radiosender sender = new Radiosender("Cloud-FM", 101.7, programm);

        LocalTime acht = LocalTime.of(8, 0);
        LocalTime neun = LocalTime.of(9, 0);
        LocalTime zehn = LocalTime.of(10, 0);
        Radiosendung morgenshow = new Radiosendung("Morgenshow", "Marcel Schachner", acht, neun, 900);
        Radiosendung vormittag = new Radiosendung("Vormittag", "Max Mustermann", neun, zehn, 3600);
        programm.sendungHinzufuegen(vormittag);
        programm.sendungHinzufuegen(morgenshow);

        System.out.println(
                sender.showName()
                        + " auf "
                        + sender.showFrequenz()
                        + " MHz, Programm vom "
                        + programm.showDatum());
        for (Radiosendung sendung : programm.showSendungen()) {
            System.out.println(
                    "  "
                            + sendung.showStartzeit()
                            + " - "
                            + sendung.showEndzeit()
                            + " "
                            + sendung.showName()
                            + " mit "
                            + sendung.showModerator());
        }

        try {
            Radiosendung doppelt = new Radiosendung("Doppelt", "Max Mustermann", acht, neun, 600);
            programm.sendungHinzufuegen(doppelt);
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        Moderation begruessung =
                new Moderation("Begrüßung", 60, "Guten Morgen, hier ist Cloud-FM mit Marcel!");
        Musikbeitrag song =
                new Musikbeitrag("Wolkenlos", 240, "Die Wolken", "Himmelblau", Musikgenre.POP);
        Werbebeitrag werbung = new Werbebeitrag("Kaffee-Spot", 30, "Bohnenglück GmbH", 5000.0);
        Musikbeitrag rock =
                new Musikbeitrag("Gewitter", 360, "Donnerhall", "Sturmfront", Musikgenre.ROCK);
        morgenshow.beitragHinzufuegen(begruessung);
        morgenshow.beitragHinzufuegen(song);
        morgenshow.beitragHinzufuegen(werbung);
        morgenshow.beitragHinzufuegen(rock);
        zeigeSendeplan(morgenshow);

        try {
            Musikbeitrag zuLang =
                    new Musikbeitrag("Endlos", 600, "Die Wolken", "Himmelblau", Musikgenre.POP);
            morgenshow.beitragHinzufuegen(zuLang);
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        System.out.println("Gewitter wird auf 300 Sekunden gekürzt, Kaffee-Spot gelöscht:");
        morgenshow.beitragKuerzen(rock, 300);
        morgenshow.beitragLoeschen(werbung);
        zeigeSendeplan(morgenshow);

        System.out.println("Gewitter wird an den Anfang verschoben:");
        morgenshow.beitragVerschieben(rock, 0);
        zeigeSendeplan(morgenshow);

        System.out.println("Eilmeldung bei Laufzeit 400 Sekunden:");
        Eilmeldung eilmeldung = new Eilmeldung("Sturmwarnung", 360, 4, "Kärnten");
        morgenshow.eilmeldungEinfuegen(eilmeldung, 400);
        zeigeSendeplan(morgenshow);
    }

    private static void zeigeSendeplan(Radiosendung sendung) {
        System.out.println("Sendeplan " + sendung.showName() + ":");
        for (Radiobeitrag beitrag : sendung.showBeitraege()) {
            System.out.println(
                    "  ab "
                            + beitrag.showStartzeitpunkt()
                            + " s: "
                            + beitrag.showTitel()
                            + " ("
                            + beitrag.showDauer()
                            + " s)");
        }
        System.out.println(
                "  Verplant: "
                        + sendung.berechneGesamtdauer()
                        + " s, Restzeit: "
                        + sendung.berechneRestzeit()
                        + " s");
    }
}
