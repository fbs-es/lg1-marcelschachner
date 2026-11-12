package de.bestellsystem.versand.domain;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Versandpaket einer Bestellung (D-12, D-22); Teil der Bestellung. */
@Entity
@Table(name = "versandpaket")
public class Versandpaket {

    private static final Pattern SENDUNGSNUMMER = Pattern.compile("[A-Za-z0-9]{1,50}");

    @Id private UUID id;

    @Enumerated(EnumType.STRING)
    private Logistikdienstleister dienstleister;

    private String sendungsnummer;

    @Enumerated(EnumType.STRING)
    private Lieferstatus lieferstatus;

    private Instant angelegtAm;

    private Instant statusGeaendertAm;

    protected Versandpaket() {}

    /** Neues Paket mit Status ANGELEGT (US-07.1). */
    public Versandpaket(
            Logistikdienstleister dienstleister, String sendungsnummer, Instant zeitpunkt) {
        if (dienstleister == null) {
            throw new IllegalArgumentException("dienstleister ist erforderlich");
        }
        this.id = UUID.randomUUID();
        this.dienstleister = dienstleister;
        this.sendungsnummer = normalisiere(sendungsnummer);
        this.lieferstatus = Lieferstatus.ANGELEGT;
        this.angelegtAm = Objects.requireNonNull(zeitpunkt, "zeitpunkt");
        this.statusGeaendertAm = zeitpunkt;
    }

    /** Getrimmt und großgeschrieben, 1–50 Buchstaben oder Ziffern (D-22). */
    public static String normalisiere(String sendungsnummer) {
        if (sendungsnummer == null) {
            throw new IllegalArgumentException("sendungsnummer ist erforderlich");
        }
        String getrimmt = sendungsnummer.strip();
        // Vor dem Großschreiben prüfen: toUpperCase macht aus "ß" sonst "SS".
        if (!SENDUNGSNUMMER.matcher(getrimmt).matches()) {
            throw new IllegalArgumentException(
                    "sendungsnummer muss aus 1 bis 50 Buchstaben (A–Z) oder Ziffern bestehen");
        }
        return getrimmt.toUpperCase(Locale.ROOT);
    }

    /** Nur erlaubte Übergänge (D-22), sonst 422. */
    public void aendereStatus(Lieferstatus neuerStatus, Instant zeitpunkt) {
        if (neuerStatus == null) {
            throw new IllegalArgumentException("lieferstatus ist erforderlich");
        }
        if (!lieferstatus.kannWechselnZu(neuerStatus)) {
            throw new FachlicheAblehnungException(
                    "Der Lieferstatus eines Pakets kann nicht von %s nach %s wechseln."
                            .formatted(lieferstatus, neuerStatus));
        }
        lieferstatus = neuerStatus;
        statusGeaendertAm = Objects.requireNonNull(zeitpunkt, "zeitpunkt");
    }

    public List<Lieferstatus> erlaubteFolgestatus() {
        return lieferstatus.erlaubteFolgestatus();
    }

    public UUID getId() {
        return id;
    }

    public Logistikdienstleister getDienstleister() {
        return dienstleister;
    }

    public String getSendungsnummer() {
        return sendungsnummer;
    }

    public Lieferstatus getLieferstatus() {
        return lieferstatus;
    }

    public Instant getAngelegtAm() {
        return angelegtAm;
    }

    public Instant getStatusGeaendertAm() {
        return statusGeaendertAm;
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes || (anderes instanceof Versandpaket paket && id.equals(paket.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
