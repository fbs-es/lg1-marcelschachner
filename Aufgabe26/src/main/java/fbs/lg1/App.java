package fbs.lg1;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Demoprogramm für den Radiosender.
 *
 * <p>Legt Sender, Programm, Moderator und eine Sendung an, füllt den Sendeplan mit Beiträgen,
 * lehnt einen zu langen Beitrag ab, kürzt und verschiebt Beiträge, fügt eine Eilmeldung mitten
 * in einen Beitrag ein, entfernt einen Beitrag und zeigt zwei abgelehnte Eingaben. Alle Ausgaben
 * erfolgen auf Deutsch.
 */
public class App {

    /**
     * Startet die Demo.
     *
     * @param args wird nicht verwendet
     */
    public static void main(String[] args) {
        Radioprogramm programm = new Radioprogramm(LocalDate.of(2026, 10, 20));
        Radiosender sender = new Radiosender("Cloud-FM", 101.5, programm);
        Moderator moderator = new Moderator("Marcel Schachner");
        Radiosendung sendung =
                new Radiosendung(
                        "Morgenshow", LocalTime.of(8, 0), LocalTime.of(9, 0), 900, moderator);
        programm.sendungHinzufuegen(sendung);

        System.out.printf(
                "%s auf %.1f MHz, Programm vom %s mit %d Sendung(en)%n",
                sender.showName(),
                sender.showFrequenz(),
                programm.showDatum(),
                programm.showSendungen().size());
        System.out.println(
                "Sendung "
                        + sendung.showName()
                        + " ("
                        + sendung.showBeginn()
                        + " - "
                        + sendung.showEnde()
                        + "), Moderator: "
                        + moderator.showName()
                        + ", Maximaldauer: "
                        + sendung.showMaxDauer()
                        + " s");

        Moderation begruessung = new Moderation("Begrüßung", 60, "Guten Morgen, Cloud-FM!");
        Musikbeitrag song = new Musikbeitrag("Morning Song", 240, "The Band", "Sunrise", "Pop");
        Werbebeitrag werbung = new Werbebeitrag("Autohaus-Spot", 30, "Autohaus Klagenfurt", 1500.0);
        Musikbeitrag hit = new Musikbeitrag("Big Hit", 300, "The Stars", "Gold", "Rock");
        sendung.beitragHinzufuegen(begruessung);
        sendung.beitragHinzufuegen(song);
        sendung.beitragHinzufuegen(werbung);
        sendung.beitragHinzufuegen(hit);
        sendeplanAusgeben(sendung);

        Musikbeitrag langerSong =
                new Musikbeitrag("Langer Song", 600, "The Band", "Sunrise", "Rock");
        boolean hinzugefuegt = sendung.beitragHinzufuegen(langerSong);
        System.out.println("Langer Song hinzugefügt: " + hinzugefuegt);

        sendung.beitragKuerzen(hit, 200);
        System.out.println("Big Hit auf 200 s gekürzt.");
        sendung.beitragVerschieben(werbung, 3);
        System.out.println("Werbung ans Ende verschoben.");
        sendeplanAusgeben(sendung);

        Eilmeldung eilmeldung = new Eilmeldung("Unwetterwarnung", 400, 5, "Kärnten");
        System.out.println("Eilmeldung bei Sekunde 150:");
        sendung.eilmeldungEinfuegen(eilmeldung, 150);
        sendeplanAusgeben(sendung);

        sendung.beitragEntfernen(werbung);
        System.out.println("Werbung entfernt.");
        sendeplanAusgeben(sendung);

        try {
            sendung.beitragKuerzen(song, 500);
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        try {
            new Eilmeldung("Test", 30, 7, "Wien");
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler: " + e.getMessage());
        }
    }

    private static void sendeplanAusgeben(Radiosendung sendung) {
        System.out.println("Sendeplan " + sendung.showName() + ":");
        for (Radiobeitrag beitrag : sendung.showBeitraege()) {
            System.out.printf(
                    "  %4d s - %4d s: %s%n",
                    beitrag.showStartzeit(), beitrag.showEndzeit(), beitrag.showTitel());
        }
        System.out.println(
                "  Gesamtdauer: "
                        + sendung.berechneGesamtdauer()
                        + " s, Restzeit: "
                        + sendung.berechneRestzeit()
                        + " s");
    }
}
