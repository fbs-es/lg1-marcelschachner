package fbs.lg1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Eine Bestellung eines {@link Kunde}n mit mindestens einer {@link Bestellposition}. */
public class Bestellung {

    private final int bestellnummer;
    private final Date bestelldatum;
    private final Kunde kunde;
    private final List<Bestellposition> positionen = new ArrayList<>();
    private Status status;

    /**
     * Legt eine offene Bestellung mit einer ersten Position an.
     *
     * @throws IllegalArgumentException wenn eine Angabe fehlt oder ungültig ist
     */
    public Bestellung(int bestellnummer, Date bestelldatum, Kunde kunde, Produkt produkt, int menge) {
        if (bestellnummer <= 0) {
            throw new IllegalArgumentException("Bestellnummer muss größer als 0 sein!");
        }
        if (bestelldatum == null) {
            throw new IllegalArgumentException("Bestelldatum darf nicht fehlen!");
        }
        if (kunde == null) {
            throw new IllegalArgumentException("Kunde darf nicht fehlen!");
        }
        if (produkt == null) {
            throw new IllegalArgumentException("Produkt darf nicht fehlen!");
        }
        if (menge <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein!");
        }

        this.bestellnummer = bestellnummer;
        this.bestelldatum = bestelldatum;
        this.kunde = kunde;
        this.status = Status.OFFEN;

        Bestellposition position = erstellePosition(produkt, menge);
        positionen.add(position);

        assert positionen.size() == 1 : "Fehler: Erste Position wurde nicht gespeichert!";
        checkInvariant();
    }

    /**
     * Fügt eine Position zum aktuellen Produktpreis hinzu.
     *
     * @return die neue Position
     * @throws IllegalStateException wenn die Bestellung nicht offen ist
     * @throws IllegalArgumentException wenn Produkt fehlt oder Menge ungültig ist
     */
    public Bestellposition positionHinzufuegen(Produkt produkt, int menge) {
        if (status != Status.OFFEN) {
            throw new IllegalStateException("Bestellung ist nicht mehr offen!");
        }
        if (produkt == null) {
            throw new IllegalArgumentException("Produkt darf nicht fehlen!");
        }
        if (menge <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein!");
        }

        int oldAnzahl = positionen.size();

        Bestellposition position = erstellePosition(produkt, menge);
        positionen.add(position);

        assert positionen.size() == oldAnzahl + 1 : "Fehler: Position wurde nicht gespeichert!";
        checkInvariant();
        return position;
    }

    /**
     * Storniert eine offene Bestellung und veranlasst vorher die Rückerstattung.
     *
     * @return {@code true}, wenn storniert wurde; {@code false}, wenn nicht offen
     */
    public boolean stornieren() {
        if (status != Status.OFFEN) {
            return false;
        }

        rueckerstattung();
        status = Status.STORNIERT;

        assert status == Status.STORNIERT : "Fehler: Bestellung wurde nicht storniert!";
        checkInvariant();
        return true;
    }

    /**
     * Setzt den Status auf {@link Status#VERSENDET}.
     *
     * @throws IllegalStateException wenn die Bestellung nicht offen ist
     */
    public void versenden() {
        if (status != Status.OFFEN) {
            throw new IllegalStateException("Bestellung kann nicht mehr versendet werden!");
        }

        status = Status.VERSENDET;

        assert status == Status.VERSENDET : "Fehler: Bestellung wurde nicht versendet!";
        checkInvariant();
    }

    /**
     * Summiert alle Positionswerte zu historischen Preisen.
     *
     * @return der Gesamtwert in Euro, auf Cent gerundet
     */
    public double berechneGesamtwert() {
        double summe = 0.0;
        for (Bestellposition position : positionen) {
            summe += position.berechnePositionswert();
        }
        return runden(summe);
    }

    private Bestellposition erstellePosition(Produkt produkt, int menge) {
        int positionsnummer = positionen.size() + 1;
        double einzelpreis = produkt.showEinzelpreis();
        return new Bestellposition(positionsnummer, menge, einzelpreis, produkt);
    }

    private void rueckerstattung() {
        double betrag = berechneGesamtwert();
        System.out.println(
                "Rückerstattung von "
                        + betrag
                        + " € für Bestellung "
                        + bestellnummer
                        + " an "
                        + kunde.showEmail()
                        + " veranlasst.");
    }

    private double runden(double betrag) {
        return Math.round(betrag * 100) / 100.0;
    }

    private void checkInvariant() {
        if (bestellnummer <= 0) {
            throw new IllegalStateException("Bestellnummer muss größer als 0 sein!");
        }
        if (bestelldatum == null || kunde == null || status == null) {
            throw new IllegalStateException("Pflichtangaben der Bestellung fehlen!");
        }
        if (positionen.isEmpty()) {
            throw new IllegalStateException("Bestellung braucht mindestens eine Position!");
        }
        int erwartet = 1;
        for (Bestellposition position : positionen) {
            if (position.showPositionsnummer() != erwartet) {
                throw new IllegalStateException("Positionsnummern sind nicht fortlaufend!");
            }
            erwartet++;
        }
    }

    /** @return die Bestellnummer */
    public int showBestellnummer() {
        return bestellnummer;
    }

    /** @return das Bestelldatum */
    public Date showBestelldatum() {
        return bestelldatum;
    }

    /** @return der Kunde */
    public Kunde showKunde() {
        return kunde;
    }

    /** @return der aktuelle Status */
    public Status showStatus() {
        return status;
    }

    /** @return alle Positionen, nicht verändern */
    public List<Bestellposition> showPositionen() {
        return positionen;
    }
}
