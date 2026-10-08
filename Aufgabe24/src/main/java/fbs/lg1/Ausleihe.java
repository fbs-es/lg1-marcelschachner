package fbs.lg1;

import java.util.Date;

/**
 * Eine einzelne Ausleihe eines {@link Buch}s durch einen {@link Nutzer}.
 *
 * <p>Die Leihfrist beträgt 14 Tage und kann genau einmal um 14 Tage verlängert werden, solange die
 * Ausleihe {@link AusleihStatus#AKTIV} ist. {@link #pruefeUeberzogen(Date)} setzt den Status auf
 * {@link AusleihStatus#UEBERZOGEN}, sobald das Rückgabedatum überschritten ist. Nach der Rückgabe
 * ist der Status {@link AusleihStatus#ZURUECKGEGEBEN}; bei Verspätung kostet jeder angefangene Tag
 * 0,50 € Strafe.
 */
public class Ausleihe {

    private static final long MILLIS_PRO_TAG = 24L * 60 * 60 * 1000;
    private static final int LEIHFRIST_TAGE = 14;
    private static final int VERLAENGERUNG_TAGE = 14;
    private static final double STRAFE_PRO_TAG = 0.5;

    private final Date ausleihdatum;
    private final Buch buch;
    private final Nutzer nutzer;
    private Date rueckgabedatum;
    private AusleihStatus status;
    private boolean verlaengert;
    private double strafe;

    /**
     * Startet eine aktive Ausleihe und markiert das Buch als ausgeliehen.
     *
     * @throws IllegalArgumentException wenn eine Angabe fehlt
     * @throws IllegalStateException wenn das Buch nicht verfügbar ist
     */
    public Ausleihe(Date ausleihdatum, Nutzer nutzer, Buch buch) {
        if (ausleihdatum == null) {
            throw new IllegalArgumentException("Ausleihdatum darf nicht fehlen!");
        }
        if (nutzer == null) {
            throw new IllegalArgumentException("Nutzer darf nicht fehlen!");
        }
        if (buch == null) {
            throw new IllegalArgumentException("Buch darf nicht fehlen!");
        }
        if (!buch.istVerfuegbar()) {
            throw new IllegalStateException("Buch ist nicht verfügbar!");
        }

        this.ausleihdatum = ausleihdatum;
        this.nutzer = nutzer;
        this.buch = buch;
        this.rueckgabedatum = plusTage(ausleihdatum, LEIHFRIST_TAGE);
        this.status = AusleihStatus.AKTIV;
        this.verlaengert = false;
        this.strafe = 0.0;

        buch.ausleihen();

        assert !buch.istVerfuegbar() : "Fehler: Buch wurde nicht ausgeliehen!";
        checkInvariant();
    }

    /**
     * Verschiebt das Rückgabedatum einmalig um 14 Tage nach hinten.
     *
     * @throws IllegalStateException wenn die Ausleihe nicht aktiv ist oder bereits verlängert wurde
     */
    public void verlaengern() {
        if (status != AusleihStatus.AKTIV) {
            throw new IllegalStateException("Nur aktive Ausleihen können verlängert werden!");
        }
        if (verlaengert) {
            throw new IllegalStateException("Ausleihe wurde bereits verlängert!");
        }

        Date oldRueckgabedatum = rueckgabedatum;

        rueckgabedatum = plusTage(oldRueckgabedatum, VERLAENGERUNG_TAGE);
        verlaengert = true;

        assert rueckgabedatum.after(oldRueckgabedatum) : "Fehler: Rückgabedatum wurde nicht verschoben!";
        assert verlaengert : "Fehler: Verlängerung wurde nicht vermerkt!";
        checkInvariant();
    }

    /**
     * Setzt den Status einer aktiven Ausleihe auf {@link AusleihStatus#UEBERZOGEN}, wenn das
     * aktuelle Datum nach dem Rückgabedatum liegt.
     *
     * @param aktuellesDatum das Datum, zu dem geprüft wird
     * @return {@code true}, wenn die Ausleihe überzogen ist
     * @throws IllegalArgumentException wenn das Datum fehlt
     */
    public boolean pruefeUeberzogen(Date aktuellesDatum) {
        if (aktuellesDatum == null) {
            throw new IllegalArgumentException("Aktuelles Datum darf nicht fehlen!");
        }

        if (status == AusleihStatus.AKTIV && aktuellesDatum.after(rueckgabedatum)) {
            status = AusleihStatus.UEBERZOGEN;
        }

        assert status != AusleihStatus.AKTIV || !aktuellesDatum.after(rueckgabedatum)
                : "Fehler: Überzogene Ausleihe ist noch aktiv!";
        checkInvariant();
        return status == AusleihStatus.UEBERZOGEN;
    }

    /**
     * Beendet die Ausleihe, macht das Buch wieder verfügbar und hinterlegt eine eventuelle Strafe.
     *
     * @param aktuellesDatum das Rückgabedatum, nicht vor dem Ausleihdatum
     * @throws IllegalArgumentException wenn das Datum fehlt oder vor dem Ausleihdatum liegt
     * @throws IllegalStateException wenn die Ausleihe bereits zurückgegeben wurde
     */
    public void zurueckgeben(Date aktuellesDatum) {
        if (status == AusleihStatus.ZURUECKGEGEBEN) {
            throw new IllegalStateException("Ausleihe wurde bereits zurückgegeben!");
        }
        if (aktuellesDatum == null) {
            throw new IllegalArgumentException("Aktuelles Datum darf nicht fehlen!");
        }
        if (aktuellesDatum.before(ausleihdatum)) {
            throw new IllegalArgumentException("Aktuelles Datum darf nicht vor dem Ausleihdatum liegen!");
        }

        strafe = strafeBerechnen(aktuellesDatum);
        status = AusleihStatus.ZURUECKGEGEBEN;
        buch.zurueckgeben();

        assert status == AusleihStatus.ZURUECKGEGEBEN : "Fehler: Ausleihe wurde nicht zurückgegeben!";
        assert buch.istVerfuegbar() : "Fehler: Buch wurde nicht zurückgegeben!";
        checkInvariant();
    }

    private double strafeBerechnen(Date aktuellesDatum) {
        long verspaetung = aktuellesDatum.getTime() - rueckgabedatum.getTime();
        if (verspaetung <= 0) {
            return 0.0;
        }
        long tage = (verspaetung + MILLIS_PRO_TAG - 1) / MILLIS_PRO_TAG;
        return tage * STRAFE_PRO_TAG;
    }

    private Date plusTage(Date datum, int tage) {
        long millis = datum.getTime() + tage * MILLIS_PRO_TAG;
        return new Date(millis);
    }

    private void checkInvariant() {
        if (ausleihdatum == null || rueckgabedatum == null || buch == null || nutzer == null || status == null) {
            throw new IllegalStateException("Pflichtangaben der Ausleihe fehlen!");
        }
        if (!rueckgabedatum.after(ausleihdatum)) {
            throw new IllegalStateException("Rückgabedatum muss nach dem Ausleihdatum liegen!");
        }
        if (strafe < 0) {
            throw new IllegalStateException("Strafe darf nicht negativ sein!");
        }
        if (status != AusleihStatus.ZURUECKGEGEBEN && buch.istVerfuegbar()) {
            throw new IllegalStateException("Buch einer laufenden Ausleihe darf nicht verfügbar sein!");
        }
    }

    /** @return das Ausleihdatum */
    public Date showAusleihdatum() {
        return ausleihdatum;
    }

    /** @return das geplante Rückgabedatum */
    public Date showRueckgabedatum() {
        return rueckgabedatum;
    }

    /** @return der aktuelle Status */
    public AusleihStatus showStatus() {
        return status;
    }

    /** @return die Verspätungsstrafe in Euro, {@code 0.0} wenn keine angefallen ist */
    public double showStrafe() {
        return strafe;
    }

    /** @return das ausgeliehene Buch */
    public Buch showBuch() {
        return buch;
    }

    /** @return der ausleihende Nutzer */
    public Nutzer showNutzer() {
        return nutzer;
    }

    /** @return {@code true}, wenn die Leihfrist bereits verlängert wurde */
    public boolean istVerlaengert() {
        return verlaengert;
    }
}
