package de.bestellsystem.zahlung.domain;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Zahlungsbuchung einer Bestellung (D-02, D-05); Teil der Bestellung. */
@Entity
@Table(name = "zahlung")
public class Zahlung {

    @Id private UUID id;

    @Enumerated(EnumType.STRING)
    private Zahlungsart zahlungsart;

    @Enumerated(EnumType.STRING)
    private Buchungsart buchungsart;

    @Enumerated(EnumType.STRING)
    private Transaktionsstatus transaktionsstatus;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "betrag_cent"))
    private Geld betrag;

    // Bleibt leer, bis Geld tatsächlich fließt (D-17).
    private Instant zahlungszeitpunkt;

    protected Zahlung() {}

    private Zahlung(
            Zahlungsart zahlungsart,
            Buchungsart buchungsart,
            Transaktionsstatus transaktionsstatus,
            Geld betrag) {
        if (zahlungsart == null) {
            throw new IllegalArgumentException("zahlungsart ist erforderlich");
        }
        if (betrag == null || betrag.istNegativ()) {
            throw new IllegalArgumentException("betrag muss angegeben und ≥ 0 sein");
        }
        this.id = UUID.randomUUID();
        this.zahlungsart = zahlungsart;
        this.buchungsart = buchungsart;
        this.transaktionsstatus = transaktionsstatus;
        this.betrag = betrag;
    }

    /** Erste Zahlung beim Checkout (D-02). */
    public static Zahlung ausstehend(Zahlungsart zahlungsart, Geld betrag) {
        return new Zahlung(zahlungsart, Buchungsart.ZAHLUNG, Transaktionsstatus.AUSSTEHEND, betrag);
    }

    /** Teilzahlung oder Erstattung (D-19): immer AUSSTEHEND, Betrag größer 0. */
    public static Zahlung ausstehend(
            Zahlungsart zahlungsart, Buchungsart buchungsart, Geld betrag) {
        if (buchungsart == null) {
            throw new IllegalArgumentException("buchungsart ist erforderlich");
        }
        if (betrag == null || betrag.compareTo(Geld.NULL) <= 0) {
            throw new IllegalArgumentException("betrag muss größer 0 sein");
        }
        return new Zahlung(zahlungsart, buchungsart, Transaktionsstatus.AUSSTEHEND, betrag);
    }

    /** Nur aus AUSSTEHEND in einen Endzustand (D-19); der Zeitpunkt wird nur bei Erfolg gesetzt. */
    public void aendereStatus(Transaktionsstatus neuerStatus, Instant zeitpunkt) {
        if (neuerStatus == null) {
            throw new IllegalArgumentException("transaktionsstatus ist erforderlich");
        }
        if (transaktionsstatus != Transaktionsstatus.AUSSTEHEND
                || neuerStatus == Transaktionsstatus.AUSSTEHEND) {
            throw new FachlicheAblehnungException(
                    "Der Status einer Buchung kann nicht von %s nach %s wechseln."
                            .formatted(transaktionsstatus, neuerStatus));
        }
        transaktionsstatus = neuerStatus;
        if (neuerStatus == Transaktionsstatus.ERFOLGREICH) {
            zahlungszeitpunkt = Objects.requireNonNull(zeitpunkt, "zeitpunkt");
        }
    }

    public boolean istErfolgreich() {
        return transaktionsstatus == Transaktionsstatus.ERFOLGREICH;
    }

    public boolean istAusstehend() {
        return transaktionsstatus == Transaktionsstatus.AUSSTEHEND;
    }

    public boolean istErstattung() {
        return buchungsart == Buchungsart.ERSTATTUNG;
    }

    public UUID getId() {
        return id;
    }

    public Zahlungsart getZahlungsart() {
        return zahlungsart;
    }

    public Buchungsart getBuchungsart() {
        return buchungsart;
    }

    public Transaktionsstatus getTransaktionsstatus() {
        return transaktionsstatus;
    }

    public Geld getBetrag() {
        return betrag;
    }

    public Optional<Instant> getZahlungszeitpunkt() {
        return Optional.ofNullable(zahlungszeitpunkt);
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes || (anderes instanceof Zahlung zahlung && id.equals(zahlung.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
