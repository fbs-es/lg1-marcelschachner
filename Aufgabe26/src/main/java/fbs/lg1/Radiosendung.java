package fbs.lg1;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Eine Radiosendung mit festem Zeitblock, Maximaldauer und einem geordneten Sendeplan aus
 * {@link Radiobeitrag}en.
 *
 * <p>Die Startzeiten aller Beiträge werden nach jeder Änderung des Sendeplans neu berechnet, sodass
 * sie lückenlos aufeinander folgen. Normale Beiträge dürfen die Maximaldauer nicht überschreiten.
 * Nur eine {@link Eilmeldung} darf das Zeitlimit sprengen; dann wird der {@link Moderator}
 * gewarnt.
 */
public class Radiosendung {

    private final String name;
    private final LocalTime beginn;
    private final LocalTime ende;
    private final int maxDauer;
    private final Moderator moderator;
    private final List<Radiobeitrag> beitraege = new ArrayList<>();

    /**
     * Legt eine Sendung mit leerem Sendeplan an.
     *
     * @throws IllegalArgumentException wenn eine Angabe fehlt, das Ende nicht nach dem Beginn liegt
     *     oder die Maximaldauer nicht größer als 0 ist oder den Zeitblock überschreitet
     */
    public Radiosendung(
            String name, LocalTime beginn, LocalTime ende, int maxDauer, Moderator moderator) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (beginn == null) {
            throw new IllegalArgumentException("Beginn darf nicht fehlen!");
        }
        if (ende == null) {
            throw new IllegalArgumentException("Ende darf nicht fehlen!");
        }
        if (!ende.isAfter(beginn)) {
            throw new IllegalArgumentException("Ende muss nach dem Beginn liegen!");
        }
        if (maxDauer <= 0) {
            throw new IllegalArgumentException("Maximaldauer muss größer als 0 sein!");
        }
        Duration zeitblock = Duration.between(beginn, ende);
        if (maxDauer > zeitblock.toSeconds()) {
            throw new IllegalArgumentException("Maximaldauer darf den Zeitblock nicht überschreiten!");
        }
        if (moderator == null) {
            throw new IllegalArgumentException("Moderator darf nicht fehlen!");
        }

        this.name = name;
        this.beginn = beginn;
        this.ende = ende;
        this.maxDauer = maxDauer;
        this.moderator = moderator;

        checkInvariant();
    }

    /**
     * Hängt einen Beitrag an das Ende des Sendeplans an, sofern er noch in die Maximaldauer passt.
     *
     * @param beitrag der neue Beitrag
     * @return {@code true}, wenn der Beitrag hinzugefügt wurde, {@code false}, wenn er die
     *     Maximaldauer überschreiten würde
     * @throws IllegalArgumentException wenn der Beitrag fehlt
     * @throws IllegalStateException wenn der Beitrag bereits in der Sendung ist
     */
    public boolean beitragHinzufuegen(Radiobeitrag beitrag) {
        if (beitrag == null) {
            throw new IllegalArgumentException("Beitrag darf nicht fehlen!");
        }
        if (beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist bereits in der Sendung!");
        }
        int neueGesamtdauer = berechneGesamtdauer() + beitrag.showDauer();
        if (neueGesamtdauer > maxDauer) {
            return false;
        }

        int oldAnzahl = beitraege.size();

        beitraege.add(beitrag);
        startzeitenNeuBerechnen();

        assert beitraege.size() == oldAnzahl + 1 : "Fehler: Beitrag wurde nicht hinzugefügt!";
        assert zeitlimitPruefen() : "Fehler: Zeitlimit wurde überschritten!";
        checkInvariant();
        return true;
    }

    /**
     * Entfernt einen Beitrag aus dem Sendeplan.
     *
     * @param beitrag der zu entfernende Beitrag
     * @throws IllegalStateException wenn der Beitrag nicht in der Sendung ist
     */
    public void beitragEntfernen(Radiobeitrag beitrag) {
        if (!beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist nicht in der Sendung!");
        }

        int oldAnzahl = beitraege.size();

        beitraege.remove(beitrag);
        startzeitenNeuBerechnen();

        assert beitraege.size() == oldAnzahl - 1 : "Fehler: Beitrag wurde nicht entfernt!";
        checkInvariant();
    }

    /**
     * Verkürzt einen Beitrag der Sendung auf eine neue Dauer.
     *
     * @param beitrag der zu kürzende Beitrag
     * @param neueDauer die neue Dauer in Sekunden
     * @throws IllegalStateException wenn der Beitrag nicht in der Sendung ist
     * @throws IllegalArgumentException wenn die neue Dauer nicht größer als 0 und kürzer als die
     *     bisherige Dauer ist
     */
    public void beitragKuerzen(Radiobeitrag beitrag, int neueDauer) {
        if (!beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist nicht in der Sendung!");
        }
        if (neueDauer <= 0 || neueDauer >= beitrag.showDauer()) {
            throw new IllegalArgumentException(
                    "Neue Dauer muss größer als 0 und kürzer als die bisherige Dauer sein!");
        }

        int oldGesamtdauer = berechneGesamtdauer();

        beitrag.aendereDauer(neueDauer);
        startzeitenNeuBerechnen();

        assert berechneGesamtdauer() < oldGesamtdauer : "Fehler: Beitrag wurde nicht gekürzt!";
        checkInvariant();
    }

    /**
     * Verschiebt einen Beitrag an eine neue Position im Sendeplan.
     *
     * @param beitrag der zu verschiebende Beitrag
     * @param neuerIndex die neue Position, beginnend bei 0
     * @throws IllegalStateException wenn der Beitrag nicht in der Sendung ist
     * @throws IllegalArgumentException wenn der Index außerhalb des Sendeplans liegt
     */
    public void beitragVerschieben(Radiobeitrag beitrag, int neuerIndex) {
        if (!beitraege.contains(beitrag)) {
            throw new IllegalStateException("Beitrag ist nicht in der Sendung!");
        }
        if (neuerIndex < 0 || neuerIndex >= beitraege.size()) {
            throw new IllegalArgumentException("Index liegt außerhalb des Sendeplans!");
        }

        beitraege.remove(beitrag);
        beitraege.add(neuerIndex, beitrag);
        startzeitenNeuBerechnen();

        assert beitraege.indexOf(beitrag) == neuerIndex : "Fehler: Beitrag wurde nicht verschoben!";
        checkInvariant();
    }

    /**
     * Fügt eine Eilmeldung an einer Laufstelle ein. Läuft dort gerade ein Beitrag, wird er geteilt
     * und nach der Eilmeldung fortgesetzt. Überschreitet die Sendung danach die Maximaldauer, wird der
     * Moderator gewarnt.
     *
     * @param eilmeldung die einzufügende Eilmeldung
     * @param laufstelle die Stelle in Sekunden ab Sendungsbeginn
     * @throws IllegalArgumentException wenn die Eilmeldung fehlt oder die Laufstelle negativ ist
     * @throws IllegalStateException wenn die Eilmeldung bereits in der Sendung ist
     */
    public void eilmeldungEinfuegen(Eilmeldung eilmeldung, int laufstelle) {
        if (eilmeldung == null) {
            throw new IllegalArgumentException("Eilmeldung darf nicht fehlen!");
        }
        if (laufstelle < 0) {
            throw new IllegalArgumentException("Laufstelle darf nicht negativ sein!");
        }
        if (beitraege.contains(eilmeldung)) {
            throw new IllegalStateException("Eilmeldung ist bereits in der Sendung!");
        }

        int oldGesamtdauer = berechneGesamtdauer();

        Radiobeitrag betroffen = findBeitrag(laufstelle);
        if (betroffen == null) {
            beitraege.add(eilmeldung);
        } else if (betroffen.showStartzeit() == laufstelle) {
            int index = beitraege.indexOf(betroffen);
            beitraege.add(index, eilmeldung);
        } else {
            Radiobeitrag rest = beitragSplitten(betroffen, laufstelle);
            int index = beitraege.indexOf(betroffen) + 1;
            beitraege.add(index, eilmeldung);
            beitraege.add(index + 1, rest);
        }
        startzeitenNeuBerechnen();

        if (!zeitlimitPruefen()) {
            int ueberschreitung = berechneGesamtdauer() - maxDauer;
            moderator.warnungAnzeigen(
                    "Sendung "
                            + name
                            + " überschreitet das Zeitlimit um "
                            + ueberschreitung
                            + " Sekunden!");
        }

        assert beitraege.contains(eilmeldung) : "Fehler: Eilmeldung wurde nicht eingefügt!";
        assert berechneGesamtdauer() == oldGesamtdauer + eilmeldung.showDauer()
                : "Fehler: Gesamtdauer stimmt nicht!";
        checkInvariant();
    }

    /**
     * Summiert die Dauer aller Beiträge.
     *
     * @return die Gesamtdauer in Sekunden
     */
    public int berechneGesamtdauer() {
        int summe = 0;
        for (Radiobeitrag beitrag : beitraege) {
            summe += beitrag.showDauer();
        }
        return summe;
    }

    /**
     * Berechnet die noch freie Zeit bis zur Maximaldauer.
     *
     * @return die Restzeit in Sekunden, negativ bei Überschreitung
     */
    public int berechneRestzeit() {
        return maxDauer - berechneGesamtdauer();
    }

    private Radiobeitrag beitragSplitten(Radiobeitrag beitrag, int laufstelle) {
        int ersteDauer = laufstelle - beitrag.showStartzeit();
        int restDauer = beitrag.showDauer() - ersteDauer;
        Radiobeitrag rest = beitrag.kopieMitDauer(restDauer);
        beitrag.aendereDauer(ersteDauer);
        return rest;
    }

    private void startzeitenNeuBerechnen() {
        int startzeit = 0;
        for (Radiobeitrag beitrag : beitraege) {
            beitrag.aendereStartzeit(startzeit);
            startzeit += beitrag.showDauer();
        }
    }

    private boolean zeitlimitPruefen() {
        return berechneGesamtdauer() <= maxDauer;
    }

    private Radiobeitrag findBeitrag(int laufstelle) {
        for (Radiobeitrag beitrag : beitraege) {
            if (beitrag.showStartzeit() <= laufstelle && laufstelle < beitrag.showEndzeit()) {
                return beitrag;
            }
        }
        return null;
    }

    private void checkInvariant() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (beginn == null || ende == null || !ende.isAfter(beginn)) {
            throw new IllegalStateException("Ende muss nach dem Beginn liegen!");
        }
        if (maxDauer <= 0) {
            throw new IllegalStateException("Maximaldauer muss größer als 0 sein!");
        }
        if (moderator == null) {
            throw new IllegalStateException("Moderator darf nicht fehlen!");
        }
        int erwarteteStartzeit = 0;
        for (Radiobeitrag beitrag : beitraege) {
            if (beitrag.showStartzeit() != erwarteteStartzeit) {
                throw new IllegalStateException("Startzeiten der Beiträge sind nicht aktuell!");
            }
            erwarteteStartzeit += beitrag.showDauer();
        }
    }

    /**
     * @return der Name der Sendung
     */
    public String showName() {
        return name;
    }

    /**
     * @return die Beginnzeit
     */
    public LocalTime showBeginn() {
        return beginn;
    }

    /**
     * @return die Endzeit
     */
    public LocalTime showEnde() {
        return ende;
    }

    /**
     * @return die Maximaldauer in Sekunden
     */
    public int showMaxDauer() {
        return maxDauer;
    }

    /**
     * @return der Moderator
     */
    public Moderator showModerator() {
        return moderator;
    }

    /**
     * @return der Sendeplan in Reihenfolge, nicht veränderbar
     */
    public List<Radiobeitrag> showBeitraege() {
        return Collections.unmodifiableList(beitraege);
    }
}
