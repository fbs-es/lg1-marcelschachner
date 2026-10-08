package fbs.lg1;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Demoprogramm für die Bibliothek.
 *
 * <p>Legt eine Bibliothek mit Büchern und Nutzern an, startet Ausleihen, verlängert eine Ausleihe,
 * zeigt abgelehnte Aktionen, gibt ein Buch pünktlich und eines verspätet zurück und gibt zum
 * Schluss alle Ausleihen aus. Alle Ausgaben erfolgen auf Deutsch.
 */
public class App {

    private static final long MILLIS_PRO_TAG = 24L * 60 * 60 * 1000;
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    /**
     * Startet die Demo.
     *
     * @param args wird nicht verwendet
     */
    public static void main(String[] args) {
        Date heute = new Date();
        Bibliothek bibliothek = new Bibliothek("Stadtbibliothek", "Hauptplatz 1, 9020 Klagenfurt");
        Buch clean = bibliothek.buchHinzufuegen("978-0132350884", "Clean Code", "Robert C. Martin");
        Buch java = bibliothek.buchHinzufuegen("978-0134685991", "Effective Java", "Joshua Bloch");
        Nutzer marcel = new Nutzer(1, "Marcel Schachner", "schachner.marcel@icloud.com");
        Nutzer max = new Nutzer(2, "Max Mustermann", "max.mustermann@gmail.com");
        bibliothek.nutzerRegistrieren(marcel);
        bibliothek.nutzerRegistrieren(max);

        System.out.println(
                bibliothek.showName()
                        + ", "
                        + bibliothek.showAdresse()
                        + ": "
                        + bibliothek.showBuecher().size()
                        + " Bücher, "
                        + bibliothek.showNutzer().size()
                        + " Nutzer");

        Ausleihe ausleiheMarcel = bibliothek.ausleiheStarten(marcel, clean, heute);
        System.out.println(
                marcel.showName()
                        + " leiht "
                        + clean.showTitel()
                        + " aus, Rückgabe bis "
                        + FORMAT.format(ausleiheMarcel.showRueckgabedatum()));

        ausleiheMarcel.verlaengern();
        System.out.println(
                "Verlängert, neue Rückgabe bis "
                        + FORMAT.format(ausleiheMarcel.showRueckgabedatum()));

        try {
            ausleiheMarcel.verlaengern();
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        try {
            bibliothek.ausleiheStarten(max, clean, heute);
        } catch (IllegalStateException e) {
            System.out.println("Fehler: " + e.getMessage());
        }

        Ausleihe ausleiheMax = bibliothek.ausleiheStarten(max, java, heute);
        System.out.println(
                max.showName()
                        + " leiht "
                        + java.showTitel()
                        + " aus, Rückgabe bis "
                        + FORMAT.format(ausleiheMax.showRueckgabedatum()));

        Date spaeter = plusTage(heute, 20);
        System.out.println(
                "Am "
                        + FORMAT.format(spaeter)
                        + " überzogen: "
                        + ausleiheMax.pruefeUeberzogen(spaeter));
        ausleiheMax.zurueckgeben(spaeter);
        System.out.printf(
                "%s gibt %s zurück, Strafe %.2f €%n",
                max.showName(), java.showTitel(), ausleiheMax.showStrafe());

        Date puenktlich = plusTage(heute, 25);
        ausleiheMarcel.zurueckgeben(puenktlich);
        System.out.printf(
                "%s gibt %s zurück, Strafe %.2f €%n",
                marcel.showName(), clean.showTitel(), ausleiheMarcel.showStrafe());

        System.out.println("Ausleihen:");
        for (Ausleihe ausleihe : bibliothek.showAusleihen()) {
            System.out.printf(
                    "  %s - %s: %s, verlängert: %s, Strafe %.2f €%n",
                    ausleihe.showNutzer().showName(),
                    ausleihe.showBuch().showTitel(),
                    ausleihe.showStatus(),
                    ausleihe.istVerlaengert(),
                    ausleihe.showStrafe());
        }
    }

    private static Date plusTage(Date datum, int tage) {
        long millis = datum.getTime() + tage * MILLIS_PRO_TAG;
        return new Date(millis);
    }
}
