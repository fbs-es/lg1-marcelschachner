package de.bestellsystem.kunde.domain;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import de.bestellsystem.mahnwesen.domain.Mahnposition;
import de.bestellsystem.mahnwesen.domain.Mahnung;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Kundenkonto mit Adressbuch; Aggregatwurzel, die I-1 und I-2 durchsetzt (D-06, D-16). */
@Entity
@Table(name = "kunde")
public class Kunde {

    public static final int NAME_MAXIMALE_LAENGE = 100;

    @Id private UUID id;

    // Wird bei jeder Adressbuch-Änderung erhöht, damit gleichzeitige Änderungen I-1 nicht
    // gemeinsam verletzen (D-16).
    @Version private Long version;

    private String vorname;

    private String nachname;

    @Embedded
    @AttributeOverride(name = "wert", column = @Column(name = "email"))
    private Email email;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "kunde_id", nullable = false)
    @OrderColumn(name = "reihenfolge")
    private List<Adresse> adressen = new ArrayList<>();

    protected Kunde() {}

    public Kunde(
            String vorname,
            String nachname,
            Email email,
            Anschrift lieferadresse,
            boolean istAuchRechnungsadresse) {
        this.id = UUID.randomUUID();
        this.vorname = pruefeName(vorname, "vorname");
        this.nachname = pruefeName(nachname, "nachname");
        if (email == null) {
            throw new IllegalArgumentException("email ist erforderlich");
        }
        this.email = email;
        if (lieferadresse == null) {
            throw new IllegalArgumentException("Eine Lieferadresse ist erforderlich");
        }
        adressen.add(new Adresse(lieferadresse, true, istAuchRechnungsadresse));
    }

    public Adresse fuegeAdresseHinzu(
            Anschrift anschrift, boolean istLieferadresse, boolean istRechnungsadresse) {
        Adresse adresse = new Adresse(anschrift, istLieferadresse, istRechnungsadresse);
        adressen.add(adresse);
        return adresse;
    }

    public Adresse aendereAdresse(
            UUID adressId,
            Anschrift anschrift,
            boolean istLieferadresse,
            boolean istRechnungsadresse) {
        Adresse adresse = adresse(adressId);
        Adresse.pruefe(anschrift, istLieferadresse, istRechnungsadresse);
        if (!istLieferadresse && istEinzigeLieferadresse(adresse)) {
            throw new FachlicheAblehnungException(
                    "Die letzte Lieferadresse muss Lieferadresse bleiben (I-1).");
        }
        adresse.aendere(anschrift, istLieferadresse, istRechnungsadresse);
        return adresse;
    }

    public void entferneAdresse(UUID adressId) {
        Adresse adresse = adresse(adressId);
        if (istEinzigeLieferadresse(adresse)) {
            throw new FachlicheAblehnungException(
                    "Die letzte Lieferadresse kann nicht gelöscht werden (I-1).");
        }
        adressen.remove(adresse);
    }

    public Adresse adresse(UUID adressId) {
        return adressen.stream()
                .filter(adresse -> adresse.getId().equals(adressId))
                .findFirst()
                .orElseThrow(() ->
                        new NichtGefundenException(
                                "Adresse " + adressId + " existiert nicht im Adressbuch"));
    }

    /** Anschrift der gewählten Lieferadresse als Snapshot für eine Bestellung (D-08). */
    public Anschrift lieferanschrift(UUID adressId) {
        Adresse adresse = adresse(adressId);
        if (!adresse.istLieferadresse()) {
            throw new FachlicheAblehnungException(
                    "Die Adresse " + adressId + " ist keine Lieferadresse.");
        }
        return adresse.getAnschrift();
    }

    /**
     * Mahnt jede Bestellung, deren Mahnung am Stichtag fällig ist, und liefert eine Nachricht mit
     * allen gemahnten Bestellungen; leer ohne Rückstand (US-06.1, D-03, D-20).
     *
     * @param bestellungen Bestellungen dieses Kunden, mit Positionen, Zahlungen und Mahnungen
     */
    public Optional<Mahnbenachrichtigung> fuehreMahnpruefungDurch(
            LocalDate stichtag, List<Bestellung> bestellungen, Period mahnintervall) {
        if (!stichtag.plus(mahnintervall).isAfter(stichtag)) {
            throw new IllegalArgumentException(
                    "Das Mahnintervall muss mindestens einen Tag betragen");
        }
        bestellungen.stream()
                .filter(bestellung -> !bestellung.getKundeId().equals(id))
                .findFirst()
                .ifPresent(fremde -> {
                    throw new IllegalArgumentException(
                            "Bestellung %s gehört nicht zum Kunden %s"
                                    .formatted(fremde.getBestellnummer(), id));
                });
        List<Mahnposition> positionen =
                bestellungen.stream()
                        .filter(bestellung -> bestellung.istMahnungFaellig(stichtag, mahnintervall))
                        .sorted(
                                Comparator.comparing(Bestellung::getFaelligAm)
                                        .thenComparing(Bestellung::getBestellnummer))
                        .map(bestellung -> {
                            Mahnung mahnung = bestellung.mahne(stichtag, mahnintervall);
                            return new Mahnposition(
                                    bestellung.getBestellnummer(),
                                    bestellung.getFaelligAm(),
                                    mahnung.getGemahnterBetrag());
                        })
                        .toList();
        return positionen.isEmpty()
                ? Optional.empty()
                : Optional.of(new Mahnbenachrichtigung(email.wert(), stichtag, positionen));
    }

    private boolean istEinzigeLieferadresse(Adresse adresse) {
        return adresse.istLieferadresse()
                && adressen.stream().filter(Adresse::istLieferadresse).count() == 1;
    }

    public UUID getId() {
        return id;
    }

    public String getVorname() {
        return vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public Email getEmail() {
        return email;
    }

    public List<Adresse> getAdressen() {
        return Collections.unmodifiableList(adressen);
    }

    private static String pruefeName(String name, String feld) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(feld + " darf nicht leer sein");
        }
        String getrimmt = name.strip();
        if (getrimmt.length() > NAME_MAXIMALE_LAENGE) {
            throw new IllegalArgumentException(
                    feld + " darf höchstens " + NAME_MAXIMALE_LAENGE + " Zeichen haben");
        }
        return getrimmt;
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes || (anderes instanceof Kunde kunde && id.equals(kunde.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
