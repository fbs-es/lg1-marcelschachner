package de.bestellsystem.katalog.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import java.util.UUID;

/** Artikel im Katalog mit Listenpreis und Lagerbestand (E02, D-09, D-15). */
@Entity
@Table(name = "produkt")
public class Produkt {

    public static final int TITEL_MAXIMALE_LAENGE = 200;

    @Id private UUID id;

    // Wrapper-Typ: null kennzeichnet für Spring Data ein neues Produkt trotz vergebener UUID.
    @Version private Long version;

    private String titel;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "listenpreis_cent"))
    private Geld listenpreis;

    private int lagerbestand;

    protected Produkt() {}

    public Produkt(String titel, Geld listenpreis, int lagerbestand) {
        this.id = UUID.randomUUID();
        this.titel = pruefeTitel(titel);
        this.listenpreis = pruefeListenpreis(listenpreis);
        this.lagerbestand = pruefeLagerbestand(lagerbestand);
    }

    public boolean istVerfuegbar() {
        return lagerbestand > 0;
    }

    public boolean istVerfuegbar(int menge) {
        if (menge < 1) {
            throw new IllegalArgumentException("menge muss mindestens 1 sein, war: " + menge);
        }
        return lagerbestand >= menge;
    }

    /** Senkt den Bestand beim Checkout; nie unter 0 (D-09). */
    public void entnehme(int menge) {
        if (!istVerfuegbar(menge)) {
            throw new KonfliktException(
                    "Nicht genug Bestand für %s: verfügbar %d, bestellt %d"
                            .formatted(titel, lagerbestand, menge));
        }
        lagerbestand -= menge;
    }

    /** Neue Preise gelten nur für künftige Bestellungen (D-10, D-23). */
    public void aendereStammdaten(String titel, Geld listenpreis) {
        String neuerTitel = pruefeTitel(titel);
        Geld neuerListenpreis = pruefeListenpreis(listenpreis);
        this.titel = neuerTitel;
        this.listenpreis = neuerListenpreis;
    }

    public void setzeLagerbestand(int lagerbestand) {
        this.lagerbestand = pruefeLagerbestand(lagerbestand);
    }

    public UUID getId() {
        return id;
    }

    public String getTitel() {
        return titel;
    }

    public Geld getListenpreis() {
        return listenpreis;
    }

    public int getLagerbestand() {
        return lagerbestand;
    }

    private static String pruefeTitel(String titel) {
        if (titel == null || titel.isBlank()) {
            throw new IllegalArgumentException("titel darf nicht leer sein");
        }
        String getrimmt = titel.strip();
        if (getrimmt.length() > TITEL_MAXIMALE_LAENGE) {
            throw new IllegalArgumentException(
                    "titel darf höchstens " + TITEL_MAXIMALE_LAENGE + " Zeichen haben");
        }
        return getrimmt;
    }

    private static Geld pruefeListenpreis(Geld listenpreis) {
        if (listenpreis == null || listenpreis.istNegativ()) {
            throw new IllegalArgumentException("listenpreis muss angegeben und ≥ 0 sein");
        }
        return listenpreis;
    }

    private static int pruefeLagerbestand(int lagerbestand) {
        if (lagerbestand < 0) {
            throw new IllegalArgumentException(
                    "lagerbestand darf nicht negativ sein, war: " + lagerbestand);
        }
        return lagerbestand;
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes
                || (anderes instanceof Produkt produkt && id.equals(produkt.getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
