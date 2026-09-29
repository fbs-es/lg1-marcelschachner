package fbs.lg1;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Ein Kunde des Scooterverleihs mit Guthaben und Ausleihhistorie.
 *
 * <p><b>Regeln:</b>
 * <ul>
 *   <li>Das Startguthaben beträgt mindestens 10 &euro;.</li>
 *   <li>Eine Fahrt kann nur starten, wenn der Kunde nicht gesperrt ist, mindestens 1 &euro;
 *       Guthaben hat und keine andere Fahrt läuft. Zusätzlich muss der Scooter gesperrt (frei)
 *       sein und einen Akkustand über 15 % haben.</li>
 *   <li>Am Ende einer Fahrt werden die Kosten vom Guthaben abgezogen. Fällt das Guthaben unter
 *       0 &euro;, wird der Kunde gesperrt und eine Mahnung per E-Mail ausgegeben.</li>
 *   <li>Über {@link #kontoAusgleichen(double)} wird das Konto wieder aufgefüllt. Sobald das
 *       Guthaben über 0 &euro; liegt, werden Sperre und Mahnung aufgehoben.</li>
 * </ul>
 *
 * <p><b>Invarianten:</b>
 * <ul>
 *   <li>Kunden-ID und Name sind nie leer, die E-Mail-Adresse enthält ein {@code @}.</li>
 *   <li>Ein Kunde mit negativem Guthaben ist immer gesperrt.</li>
 *   <li>Sperre und offene Mahnung treten immer gemeinsam auf.</li>
 *   <li>Höchstens eine Ausleihe ist gleichzeitig offen.</li>
 * </ul>
 *
 * <p>Beträge sind in Euro und werden auf Cent gerundet. Diese Klasse ist nicht thread-sicher.
 *
 * <p>Beispiel:
 * <pre>{@code
 * Kunde kunde = new Kunde("K-001", "Erika Muster", "erika@example.com", 10.00, clock);
 * if (kunde.fahrtStarten(scooter)) {
 *     // ... Fahrt ...
 *     kunde.fahrtBeenden();
 * }
 * }</pre>
 *
 * @see Scooter
 * @see Ausleihung
 */
public class Kunde {

    private final String kundenId;
    private final String name;
    private final String email;
    private final Clock clock;
    private final List<Ausleihung> ausleihungen = new ArrayList<>();
    private double guthaben;
    private boolean istKundeGesperrt;
    private boolean mahnungOffen;

    /**
     * Legt einen neuen Kunden an. Der Kunde ist zu Beginn nicht gesperrt und hat keine Mahnung.
     *
     * @param kundenId die eindeutige Kunden-ID, nicht {@code null} und nicht leer
     * @param name     der Name des Kunden, nicht {@code null} und nicht leer
     * @param email    die E-Mail-Adresse, nicht {@code null}, muss ein {@code @} enthalten
     * @param guthaben das Startguthaben in Euro, mindestens 10,00
     * @param clock    die Zeitquelle, die an jede {@link Ausleihung} weitergegeben wird
     * @throws IllegalArgumentException wenn {@code kundenId} oder {@code name} leer ist, die
     *                                  E-Mail-Adresse ungültig ist oder das Startguthaben unter
     *                                  10 &euro; liegt
     */
    public Kunde(String kundenId, String name, String email, double guthaben, Clock clock) {
        if (kundenId == null || kundenId.isBlank()) {
            throw new IllegalArgumentException("Kunden-ID darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-Mail-Adresse ist ungültig!");
        }
        if (guthaben < 10.00) {
            throw new IllegalArgumentException("Startguthaben muss mindestens 10 € betragen!");
        }

        this.kundenId = kundenId;
        this.name = name;
        this.email = email;
        this.guthaben = guthaben;
        this.clock = clock;
        this.istKundeGesperrt = false;
        this.mahnungOffen = false;

        checkInvariant();
    }

    /**
     * Startet eine Fahrt mit dem angegebenen Scooter.
     *
     * <p>Bei erfolgreichem Start wird eine neue {@link Ausleihung} in der Historie gespeichert
     * und der Scooter entsperrt. Sind die Voraussetzungen nicht erfüllt, passiert nichts und die
     * Methode liefert {@code false}. Die Voraussetzungen führen also nie zu einer Exception.
     *
     * @throws IllegalArgumentException wenn der Kunde ohne {@code clock} ({@code null}) angelegt
     *                                  wurde und alle Voraussetzungen erfüllt sind
     *
     * @param scooter der Scooter für die Fahrt; darf {@code null} sein (Ergebnis dann {@code false})
     * @return {@code true}, wenn die Fahrt gestartet wurde. {@code false}, wenn {@code scooter}
     *         {@code null} ist, bereits eine Fahrt läuft, der Scooter nicht frei ist, sein
     *         Akkustand 15 % oder weniger beträgt, das Guthaben unter 1 &euro; liegt oder der
     *         Kunde gesperrt ist
     */
    public boolean fahrtStarten(Scooter scooter) {
        checkInvariant();
        if (scooter == null
                || findOffeneAusleihung() != null
                || !scooter.checkIstGesperrt()
                || scooter.checkAkkustand() <= 15
                || guthaben < 1.00
                || istKundeGesperrt) {
            return false;
        }

        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        ausleihungen.add(ausleihung);

        assert findOffeneAusleihung() == ausleihung : "Fehler: Ausleihe wurde nicht gespeichert!";
        assert ausleihung.showScooter() == scooter : "Fehler: Scooter wurde nicht zugewiesen!";
        assert !scooter.checkIstGesperrt() : "Fehler: Scooter wurde nicht entsperrt!";
        checkInvariant();
        return true;
    }

    /**
     * Beendet die laufende Fahrt und zieht die Kosten vom Guthaben ab.
     *
     * <p>Fällt das Guthaben dadurch unter 0 &euro;, wird der Kunde gesperrt, die Mahnung als
     * offen markiert und eine Mahnung auf der Konsole ausgegeben.
     *
     * @throws IllegalStateException wenn keine Fahrt läuft oder die Fahrt nicht beendet werden
     *                               kann (siehe {@link Ausleihung#beenden()})
     */
    public void fahrtBeenden() {
        checkInvariant();
        Ausleihung ausleihung = findOffeneAusleihung();
        if (ausleihung == null) {
            throw new IllegalStateException("Kunde hat keinen Scooter ausgeliehen!");
        }

        double oldGuthaben = guthaben;

        ausleihung.beenden();
        guthaben = runden(guthaben - ausleihung.showKosten());

        if (guthaben < 0) {
            istKundeGesperrt = true;
            mahnungOffen = true;
            mahnungSenden();
        }

        assert guthaben == runden(oldGuthaben - ausleihung.showKosten())
                : "Fehler: Fahrtkosten wurden nicht korrekt abgezogen!";
        assert ausleihung.showScooter().checkIstGesperrt()
                : "Fehler: Scooter wurde nicht gesperrt!";
        assert guthaben >= 0 || (istKundeGesperrt && mahnungOffen)
                : "Fehler: Kunde wurde trotz negativem Guthaben nicht gesperrt!";
        checkInvariant();
    }

    /**
     * Zahlt einen Betrag auf das Konto ein.
     *
     * <p>Liegt das Guthaben danach über 0 &euro;, werden Sperre und offene Mahnung aufgehoben.
     * Bei einem Guthaben von 0 &euro; oder weniger bleibt der Kunde gesperrt.
     *
     * @param betrag der eingezahlte Betrag in Euro, größer als 0
     * @throws IllegalArgumentException wenn {@code betrag} 0 oder negativ ist
     */
    public void kontoAusgleichen(double betrag) {
        checkInvariant();
        if (betrag <= 0) {
            throw new IllegalArgumentException("Betrag muss positiv sein!");
        }

        double oldGuthaben = guthaben;

        guthaben = runden(guthaben + betrag);

        if (guthaben > 0) {
            istKundeGesperrt = false;
            mahnungOffen = false;
        }

        assert guthaben == runden(oldGuthaben + betrag)
                : "Fehler: Guthaben wurde nicht korrekt erhöht!";
        assert guthaben <= 0 || (!istKundeGesperrt && !mahnungOffen)
                : "Fehler: Sperre wurde trotz positivem Guthaben nicht aufgehoben!";
        checkInvariant();
    }

    /**
     * Gibt die Mahnung an die E-Mail-Adresse des Kunden auf der Konsole aus.
     */
    private void mahnungSenden() {
        System.out.println(
                "Mahnung per E-Mail an "
                        + email
                        + ": Ihr Guthaben beträgt "
                        + guthaben
                        + " €. Ihr Konto ist gesperrt.");
    }

    /**
     * Sucht die laufende Ausleihe des Kunden.
     *
     * @return die offene Ausleihe oder {@code null}, wenn keine Fahrt läuft
     */
    private Ausleihung findOffeneAusleihung() {
        for (Ausleihung ausleihung : ausleihungen) {
            if (!ausleihung.istBeendet()) {
                return ausleihung;
            }
        }
        return null;
    }

    /**
     * Rundet einen Betrag auf Cent.
     *
     * @param betrag der Betrag in Euro
     * @return der auf zwei Nachkommastellen gerundete Betrag
     */
    private double runden(double betrag) {
        return Math.round(betrag * 100) / 100.0;
    }

    /**
     * Prüft die Klasseninvarianten.
     *
     * @throws IllegalStateException wenn Kunden-ID, Name oder E-Mail ungültig sind, ein Kunde mit
     *                               negativem Guthaben nicht gesperrt ist, Sperre und Mahnung
     *                               nicht übereinstimmen oder mehr als eine Fahrt offen ist
     */
    private void checkInvariant() {
        if (kundenId == null || kundenId.isBlank()) {
            throw new IllegalStateException("Kunden-ID darf nicht leer sein!");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name darf nicht leer sein!");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalStateException("E-Mail-Adresse ist ungültig!");
        }
        if (guthaben < 0 && !istKundeGesperrt) {
            throw new IllegalStateException("Kunde mit negativem Guthaben muss gesperrt sein!");
        }
        if (istKundeGesperrt != mahnungOffen) {
            throw new IllegalStateException("Sperre und Mahnung müssen übereinstimmen!");
        }
        int offeneFahrten = 0;
        for (Ausleihung ausleihung : ausleihungen) {
            if (!ausleihung.istBeendet()) {
                offeneFahrten++;
            }
        }
        if (offeneFahrten > 1) {
            throw new IllegalStateException(
                    "Kunde darf höchstens einen Scooter gleichzeitig ausleihen!");
        }
    }

    /**
     * Liefert die Kunden-ID.
     *
     * @return die Kunden-ID, nie {@code null} oder leer
     */
    public String showKundenId() {
        return kundenId;
    }

    /**
     * Liefert den Namen des Kunden.
     *
     * @return der Name, nie {@code null} oder leer
     */
    public String showName() {
        return name;
    }

    /**
     * Liefert die E-Mail-Adresse des Kunden.
     *
     * @return die E-Mail-Adresse
     */
    public String showEmail() {
        return email;
    }

    /**
     * Liefert das aktuelle Guthaben.
     *
     * @return das Guthaben in Euro; negativ, wenn der Kunde im Minus ist
     */
    public double showGuthaben() {
        return guthaben;
    }

    /**
     * Prüft, ob der Kunde gesperrt ist.
     *
     * @return {@code true}, wenn der Kunde keine Fahrten starten darf, weil das Guthaben
     *         aufgebraucht wurde
     */
    public boolean istKundeGesperrt() {
        return istKundeGesperrt;
    }

    /**
     * Prüft, ob eine Mahnung offen ist.
     *
     * @return {@code true}, solange der Kunde gesperrt ist und sein Konto nicht ausgeglichen hat
     */
    public boolean istMahnungOffen() {
        return mahnungOffen;
    }

    /**
     * Liefert die Ausleihhistorie des Kunden, älteste zuerst.
     *
     * <p>Die Liste ist die interne Liste des Kunden und keine Kopie. Sie darf nicht verändert
     * werden.
     *
     * @return alle Ausleihen des Kunden, laufende und beendete
     */
    public List<Ausleihung> showAusleihungen() {
        return ausleihungen;
    }
}
