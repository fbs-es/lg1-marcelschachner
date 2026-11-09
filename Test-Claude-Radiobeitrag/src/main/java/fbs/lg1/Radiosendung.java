package fbs.lg1;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Eine Radiosendung mit festem Zeitblock, Moderator und Maximaldauer in Sekunden.
 *
 * <p>Die Sendung verwaltet ihren Sendeplan als geordnete Liste von {@link Radiobeitrag}en. Der
 * Startzeitpunkt jedes Beitrags ist die Summe der Dauern aller vorherigen Beiträge und wird nach
 * jeder Änderung am Sendeplan automatisch neu berechnet. Neue Beiträge dürfen die Maximaldauer
 * nicht überschreiten; nur Eilmeldungen dürfen sie überschreiten, dann wird der Moderator gewarnt.
 */
public class Radiosendung {

    private final String name;
    private final String moderator;
    private final LocalTime startzeit;
    private final LocalTime endzeit;
    private final int maximaldauer;
    private final List<Radiobeitrag> beitraege = new ArrayList<>();

    /**
     * Legt eine Radiosendung ohne Beiträge an.
     *
     * @param name der Name der Sendung
     * @param moderator der Name des zugewiesenen Moderators
     * @param startzeit der Beginn des Zeitblocks
     * @param endzeit das Ende des Zeitblocks
     * @param maximaldauer die erlaubte Sendezeit in Sekunden
     * @throws IllegalArgumentException wenn Name oder Moderator leer sind, eine Zeit fehlt, die
     *     Endzeit nicht nach der Startzeit liegt oder die Maximaldauer nicht größer als 0 oder
     *     länger als der Zeitblock ist
     */
    public Radiosendung(
            String name, String moderator, LocalTime startzeit, LocalTime endzeit, int maximaldauer) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (moderator == null || moderator.isBlank()) {
            throw new IllegalArgumentException("Moderator darf nicht leer sein!");
        }
        if (startzeit == null) {
            throw new IllegalArgumentException("Startzeit darf nicht fehlen!");
        }
        if (endzeit == null) {
            throw new IllegalArgumentException("Endzeit darf nicht fehlen!");
        }
        if (!endzeit.isAfter(startzeit)) {
            throw new IllegalArgumentException("Endzeit muss nach der Startzeit liegen!");
        }
        if (maximaldauer <= 0) {
            throw new IllegalArgumentException("Maximaldauer muss größer als 0 sein!");
        }
        int blocklaenge = endzeit.toSecondOfDay() - startzeit.toSecondOfDay();
        if (maximaldauer > blocklaenge) {
            throw new IllegalArgumentException("Maximaldauer darf nicht länger als der Zeitblock sein!");
        }

        this.name = name;
        this.moderator = moderator;
        this.startzeit = startzeit;
        this.endzeit = endzeit;
        this.maximaldauer = maximaldauer;

        checkInvariant();
    }

    /**
     * Hängt einen Beitrag an das Ende des Sendeplans an.
     *
     * @throws IllegalArgumentException wenn der Beitrag fehlt
     * @throws IllegalStateException wenn der Beitrag bereits im Sendeplan ist oder die Sendung
     *     dadurch die Maximaldauer überschreiten würde
     */
    public void beitragHinzufuegen(Radiobeitrag beitrag) {
        if (beitrag == null) {
            throw new IllegalArgumentException("Beitrag darf nicht fehlen!");
        }
        if (beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist bereits im Sendeplan!");
        }
        int oldGesamtdauer = berechneGesamtdauer();
        int neueGesamtdauer = oldGesamtdauer + beitrag.showDauer();
        if (neueGesamtdauer > maximaldauer) {
            throw new IllegalStateException("Beitrag überschreitet die Maximaldauer der Sendung!");
        }

        beitraege.add(beitrag);
        startzeitpunkteBerechnen();

        assert beitrag.showStartzeitpunkt() == oldGesamtdauer : "Fehler: Startzeitpunkt ist falsch!";
        assert berechneGesamtdauer() == neueGesamtdauer : "Fehler: Beitrag wurde nicht hinzugefügt!";
        checkInvariant();
    }

    /**
     * Hängt einen bereits geplanten Beitrag beim Wiederaufbau eines gespeicherten Sendeplans an
     * das Ende an. Anders als {@link #beitragHinzufuegen} wird die Maximaldauer nicht geprüft, weil
     * ein gespeicherter Sendeplan nach einer Eilmeldung bereits über dem Zeitlimit liegen darf.
     *
     * @throws IllegalArgumentException wenn der Beitrag fehlt
     * @throws IllegalStateException wenn der Beitrag bereits im Sendeplan ist
     */
    public void beitragWiederherstellen(Radiobeitrag beitrag) {
        if (beitrag == null) {
            throw new IllegalArgumentException("Beitrag darf nicht fehlen!");
        }
        if (beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist bereits im Sendeplan!");
        }

        int oldGesamtdauer = berechneGesamtdauer();

        beitraege.add(beitrag);
        startzeitpunkteBerechnen();

        assert beitrag.showStartzeitpunkt() == oldGesamtdauer : "Fehler: Startzeitpunkt ist falsch!";
        assert beitraege.contains(beitrag) : "Fehler: Beitrag wurde nicht wiederhergestellt!";
        checkInvariant();
    }

    /**
     * Kürzt einen Beitrag im Sendeplan auf eine neue Dauer. Alle nachfolgenden Beiträge rücken
     * zeitlich nach vorne.
     *
     * @param beitrag der zu kürzende Beitrag
     * @param neueDauer die neue Dauer in Sekunden
     * @throws IllegalArgumentException wenn der Beitrag fehlt oder die neue Dauer nicht größer als
     *     0 und kürzer als die bisherige Dauer ist
     * @throws IllegalStateException wenn der Beitrag nicht im Sendeplan ist
     */
    public void beitragKuerzen(Radiobeitrag beitrag, int neueDauer) {
        if (beitrag == null) {
            throw new IllegalArgumentException("Beitrag darf nicht fehlen!");
        }
        if (!beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist nicht im Sendeplan!");
        }
        if (neueDauer <= 0) {
            throw new IllegalArgumentException("Dauer muss größer als 0 sein!");
        }
        if (neueDauer >= beitrag.showDauer()) {
            throw new IllegalArgumentException("Neue Dauer muss kürzer als die bisherige sein!");
        }

        int oldGesamtdauer = berechneGesamtdauer();

        beitrag.aendereDauer(neueDauer);
        startzeitpunkteBerechnen();

        assert berechneGesamtdauer() < oldGesamtdauer : "Fehler: Sendung wurde nicht kürzer!";
        checkInvariant();
    }

    /**
     * Löscht einen Beitrag aus dem Sendeplan. Alle nachfolgenden Beiträge rücken zeitlich nach
     * vorne.
     *
     * @throws IllegalArgumentException wenn der Beitrag fehlt
     * @throws IllegalStateException wenn der Beitrag nicht im Sendeplan ist
     */
    public void beitragLoeschen(Radiobeitrag beitrag) {
        if (beitrag == null) {
            throw new IllegalArgumentException("Beitrag darf nicht fehlen!");
        }
        if (!beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist nicht im Sendeplan!");
        }

        int oldAnzahl = beitraege.size();

        beitraege.remove(beitrag);
        startzeitpunkteBerechnen();

        assert beitraege.size() == oldAnzahl - 1 : "Fehler: Beitrag wurde nicht gelöscht!";
        checkInvariant();
    }

    /**
     * Verschiebt einen Beitrag an eine neue Position im Sendeplan und berechnet danach alle
     * Startzeitpunkte neu.
     *
     * @param beitrag der zu verschiebende Beitrag
     * @param neuerIndex die neue Position, beginnend bei 0
     * @throws IllegalArgumentException wenn der Beitrag fehlt oder der Index außerhalb des
     *     Sendeplans liegt
     * @throws IllegalStateException wenn der Beitrag nicht im Sendeplan ist
     */
    public void beitragVerschieben(Radiobeitrag beitrag, int neuerIndex) {
        if (beitrag == null) {
            throw new IllegalArgumentException("Beitrag darf nicht fehlen!");
        }
        if (!beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist nicht im Sendeplan!");
        }
        if (neuerIndex < 0 || neuerIndex >= beitraege.size()) {
            throw new IllegalArgumentException("Index liegt außerhalb des Sendeplans!");
        }

        int oldGesamtdauer = berechneGesamtdauer();

        beitraege.remove(beitrag);
        beitraege.add(neuerIndex, beitrag);
        startzeitpunkteBerechnen();

        assert beitraege.get(neuerIndex) == beitrag : "Fehler: Beitrag wurde nicht verschoben!";
        assert berechneGesamtdauer() == oldGesamtdauer : "Fehler: Gesamtdauer hat sich geändert!";
        checkInvariant();
    }

    /**
     * Fügt eine Eilmeldung sofort an der aktuellen Laufstelle in den Sendeplan ein.
     *
     * <p>Beginnt an der Laufstelle gerade ein Beitrag, schiebt sich die Eilmeldung direkt davor.
     * Liegt die Laufstelle mitten in einem Beitrag, wird dieser geteilt: der bereits gesendete Teil
     * bleibt stehen, danach folgt die Eilmeldung und danach eine Fortsetzung mit der Restdauer.
     * Liegt die Laufstelle hinter dem letzten Beitrag, wird die Eilmeldung angehängt. Die
     * Maximaldauer darf dabei überschritten werden; in diesem Fall wird eine deutliche Warnung für
     * den Moderator ausgegeben.
     *
     * @param eilmeldung die einzufügende Eilmeldung
     * @param laufzeit die aktuelle Laufstelle in Sekunden seit Sendungsbeginn
     * @throws IllegalArgumentException wenn die Eilmeldung fehlt oder die Laufzeit negativ ist
     * @throws IllegalStateException wenn die Eilmeldung bereits im Sendeplan ist
     */
    public void eilmeldungEinfuegen(Eilmeldung eilmeldung, int laufzeit) {
        if (eilmeldung == null) {
            throw new IllegalArgumentException("Eilmeldung darf nicht fehlen!");
        }
        if (laufzeit < 0) {
            throw new IllegalArgumentException("Laufzeit darf nicht negativ sein!");
        }
        if (beitraege.contains(eilmeldung)) {
            throw new IllegalStateException("Eilmeldung ist bereits im Sendeplan!");
        }

        int oldGesamtdauer = berechneGesamtdauer();

        Radiobeitrag aktuellerBeitrag = findBeitragBeiLaufzeit(laufzeit);
        if (aktuellerBeitrag == null) {
            beitraege.add(eilmeldung);
        } else {
            int index = beitraege.indexOf(aktuellerBeitrag);
            int bereitsGesendet = laufzeit - aktuellerBeitrag.showStartzeitpunkt();
            if (bereitsGesendet == 0) {
                beitraege.add(index, eilmeldung);
            } else {
                int restdauer = aktuellerBeitrag.showDauer() - bereitsGesendet;
                Radiobeitrag fortsetzung = aktuellerBeitrag.erstelleFortsetzung(restdauer);
                aktuellerBeitrag.aendereDauer(bereitsGesendet);
                beitraege.add(index + 1, eilmeldung);
                beitraege.add(index + 2, fortsetzung);
            }
        }
        startzeitpunkteBerechnen();

        if (istZeitlimitUeberschritten()) {
            int ueberschreitung = berechneGesamtdauer() - maximaldauer;
            System.out.println(
                    "WARNUNG an "
                            + moderator
                            + ": Sendung '"
                            + name
                            + "' überschreitet durch die Eilmeldung ihr Zeitlimit um "
                            + ueberschreitung
                            + " Sekunden!");
        }

        int erwarteteGesamtdauer = oldGesamtdauer + eilmeldung.showDauer();
        assert berechneGesamtdauer() == erwarteteGesamtdauer : "Fehler: Gesamtdauer ist falsch!";
        assert beitraege.contains(eilmeldung) : "Fehler: Eilmeldung wurde nicht eingefügt!";
        checkInvariant();
    }

    /**
     * @return die Summe der Dauern aller Beiträge in Sekunden
     */
    public int berechneGesamtdauer() {
        int summe = 0;
        for (Radiobeitrag beitrag : beitraege) {
            summe += beitrag.showDauer();
        }
        return summe;
    }

    /**
     * @return die noch verbleibende Sendezeit in Sekunden, negativ wenn das Zeitlimit
     *     überschritten ist
     */
    public int berechneRestzeit() {
        int gesamtdauer = berechneGesamtdauer();
        return maximaldauer - gesamtdauer;
    }

    private Radiobeitrag findBeitragBeiLaufzeit(int laufzeit) {
        for (Radiobeitrag beitrag : beitraege) {
            int ende = beitrag.showStartzeitpunkt() + beitrag.showDauer();
            if (laufzeit >= beitrag.showStartzeitpunkt() && laufzeit < ende) {
                return beitrag;
            }
        }
        return null;
    }

    private void startzeitpunkteBerechnen() {
        int startzeitpunkt = 0;
        for (Radiobeitrag beitrag : beitraege) {
            beitrag.aendereStartzeitpunkt(startzeitpunkt);
            startzeitpunkt += beitrag.showDauer();
        }
    }

    private void checkInvariant() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (moderator == null || moderator.isBlank()) {
            throw new IllegalStateException("Moderator darf nicht leer sein!");
        }
        if (startzeit == null || endzeit == null || !endzeit.isAfter(startzeit)) {
            throw new IllegalStateException("Zeitblock ist ungültig!");
        }
        if (maximaldauer <= 0) {
            throw new IllegalStateException("Maximaldauer muss größer als 0 sein!");
        }
        int erwarteterStart = 0;
        for (Radiobeitrag beitrag : beitraege) {
            if (beitrag == null) {
                throw new IllegalStateException("Sendeplan enthält einen fehlenden Beitrag!");
            }
            if (beitrag.showStartzeitpunkt() != erwarteterStart) {
                throw new IllegalStateException("Startzeitpunkte im Sendeplan sind nicht aktuell!");
            }
            erwarteterStart += beitrag.showDauer();
        }
    }

    /**
     * @return der Name der Sendung
     */
    public String showName() {
        return name;
    }

    /**
     * @return der Name des zugewiesenen Moderators
     */
    public String showModerator() {
        return moderator;
    }

    /**
     * @return der Beginn des Zeitblocks
     */
    public LocalTime showStartzeit() {
        return startzeit;
    }

    /**
     * @return das Ende des Zeitblocks
     */
    public LocalTime showEndzeit() {
        return endzeit;
    }

    /**
     * @return die erlaubte Sendezeit in Sekunden
     */
    public int showMaximaldauer() {
        return maximaldauer;
    }

    /**
     * @return der Sendeplan in zeitlicher Reihenfolge, nicht verändern
     */
    public List<Radiobeitrag> showBeitraege() {
        return beitraege;
    }

    /**
     * @return true, wenn die verplante Gesamtzeit die Maximaldauer überschreitet
     */
    public boolean istZeitlimitUeberschritten() {
        int gesamtdauer = berechneGesamtdauer();
        return gesamtdauer > maximaldauer;
    }
}
