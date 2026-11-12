package de.bestellsystem.bestellung.domain;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aufgegebene Bestellung; Aggregatwurzel für Positionen und Zahlungen (E04, E08).
 *
 * <p>Einzelpreise, Rabattbetrag und Lieferanschrift sind eingefroren (I-8); Warenwert und
 * Gesamtwert werden immer berechnet (D-01).
 */
@Entity
@Table(name = "bestellung")
public class Bestellung {

    @Id private String bestellnummer;

    // Wrapper-Typ: null kennzeichnet für Spring Data eine neue Bestellung trotz vergebener ID.
    @Version private Long version;

    private UUID kundeId;

    private LocalDate bestelldatum;

    private LocalDate faelligAm;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "strasse", column = @Column(name = "liefer_strasse")),
        @AttributeOverride(name = "stadt", column = @Column(name = "liefer_stadt")),
        @AttributeOverride(name = "postleitzahl", column = @Column(name = "liefer_postleitzahl")),
        @AttributeOverride(name = "land", column = @Column(name = "liefer_land"))
    })
    private Anschrift lieferanschrift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gutschein_id")
    private Gutschein gutschein;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "rabatt_betrag_cent"))
    private Geld rabattBetrag;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bestellnummer", nullable = false)
    @OrderBy("zeilennummer")
    private List<Bestellposition> positionen = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bestellnummer", nullable = false)
    private List<Zahlung> zahlungen = new ArrayList<>();

    protected Bestellung() {}

    /**
     * Checkout (US-04.1, US-08.1): prüft alles, bevor der Bestand sinkt, damit eine Ablehnung
     * nichts verändert.
     *
     * @param gutschein höchstens einer, {@code null} ohne Gutschein
     */
    public static Bestellung gibAuf(
            String bestellnummer,
            UUID kundeId,
            LocalDate bestelldatum,
            Period zahlungsziel,
            Anschrift lieferanschrift,
            List<Bestellwunsch> wuensche,
            Zahlungsart zahlungsart,
            Gutschein gutschein) {
        if (lieferanschrift == null) {
            throw new IllegalArgumentException("Eine Lieferanschrift ist erforderlich");
        }
        if (zahlungsart == null) {
            throw new IllegalArgumentException("Eine Zahlungsart ist erforderlich");
        }
        Map<Produkt, Integer> mengen = fasseZusammen(wuensche);
        if (gutschein != null && !gutschein.istGueltigAm(bestelldatum)) {
            throw new FachlicheAblehnungException(
                    "Der Gutschein " + gutschein.getCode() + " ist abgelaufen.");
        }
        pruefeBestand(mengen);

        Bestellung bestellung = new Bestellung();
        bestellung.bestellnummer = Objects.requireNonNull(bestellnummer, "bestellnummer");
        bestellung.kundeId = Objects.requireNonNull(kundeId, "kundeId");
        bestellung.bestelldatum = bestelldatum;
        bestellung.faelligAm = bestelldatum.plus(zahlungsziel);
        bestellung.lieferanschrift = lieferanschrift;
        mengen.forEach((produkt, menge) -> {
            produkt.entnehme(menge);
            bestellung.positionen.add(
                    new Bestellposition(bestellung.positionen.size() + 1, produkt, menge));
        });
        bestellung.gutschein = gutschein;
        bestellung.rabattBetrag =
                gutschein == null
                        ? Geld.NULL
                        : gutschein.berechneRabatt(bestellung.berechneWarenwert());
        bestellung.zahlungen.add(Zahlung.ausstehend(zahlungsart, bestellung.berechneGesamtwert()));
        return bestellung;
    }

    // Reihenfolge der ersten Auswahl bestimmt die Zeilennummer (D-10).
    private static Map<Produkt, Integer> fasseZusammen(List<Bestellwunsch> wuensche) {
        if (wuensche == null || wuensche.isEmpty()) {
            throw new IllegalArgumentException(
                    "Eine Bestellung braucht mindestens eine Position (I-5)");
        }
        Map<Produkt, Integer> mengen = new LinkedHashMap<>();
        wuensche.forEach(wunsch -> mengen.merge(wunsch.produkt(), wunsch.menge(), Math::addExact));
        return mengen;
    }

    private static void pruefeBestand(Map<Produkt, Integer> mengen) {
        List<String> fehlbestaende =
                mengen.entrySet().stream()
                        .filter(eintrag -> !eintrag.getKey().istVerfuegbar(eintrag.getValue()))
                        .map(eintrag ->
                                "%s (verfügbar %d, bestellt %d)"
                                        .formatted(
                                                eintrag.getKey().getTitel(),
                                                eintrag.getKey().getLagerbestand(),
                                                eintrag.getValue()))
                        .toList();
        if (!fehlbestaende.isEmpty()) {
            throw new KonfliktException(
                    "Nicht genug Bestand für: " + String.join(", ", fehlbestaende));
        }
    }

    /** Summe der Zwischenbeträge (D-01). */
    public Geld berechneWarenwert() {
        return positionen.stream()
                .map(Bestellposition::berechneZwischenbetrag)
                .reduce(Geld.NULL, Geld::addiere);
    }

    /** {@code max(0, warenwert − rabattBetrag)} (D-01). */
    public Geld berechneGesamtwert() {
        Geld gesamtwert = berechneWarenwert().subtrahiere(rabattBetrag);
        return gesamtwert.istNegativ() ? Geld.NULL : gesamtwert;
    }

    public String getBestellnummer() {
        return bestellnummer;
    }

    public UUID getKundeId() {
        return kundeId;
    }

    public LocalDate getBestelldatum() {
        return bestelldatum;
    }

    public LocalDate getFaelligAm() {
        return faelligAm;
    }

    public Anschrift getLieferanschrift() {
        return lieferanschrift;
    }

    public Optional<Gutschein> getGutschein() {
        return Optional.ofNullable(gutschein);
    }

    public Geld getRabattBetrag() {
        return rabattBetrag;
    }

    public List<Bestellposition> getPositionen() {
        return Collections.unmodifiableList(positionen);
    }

    public List<Zahlung> getZahlungen() {
        return Collections.unmodifiableList(zahlungen);
    }

    // Die Bestellnummer wird vor dem Anlegen vergeben und ändert sich nie.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes
                || (anderes instanceof Bestellung bestellung
                        && bestellnummer.equals(bestellung.getBestellnummer()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(bestellnummer);
    }
}
