package de.bestellsystem.merkliste.domain;

import de.bestellsystem.katalog.domain.Produkt;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Assoziationsklasse Kunde *–* Produkt (D-07). Angelegt wird ein Eintrag nur über {@code
 * MerklisteneintragRepository.merke}, damit doppeltes Merken auch nebenläufig ein No-op bleibt
 * (D-21); die Entity selbst ist danach unveränderlich.
 */
@Entity
@Table(name = "merklisteneintrag")
public class Merklisteneintrag {

    @EmbeddedId private MerklisteneintragId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produkt_id", insertable = false, updatable = false)
    private Produkt produkt;

    private Instant hinzugefuegtAm;

    protected Merklisteneintrag() {}

    public MerklisteneintragId getId() {
        return id;
    }

    public Produkt getProdukt() {
        return produkt;
    }

    public Instant getHinzugefuegtAm() {
        return hinzugefuegtAm;
    }

    /** Merkdatum, wie es der Kunde in der Zeitzone des Shops sieht (D-21). */
    public LocalDate merkdatum(ZoneId zeitzone) {
        return LocalDate.ofInstant(hinzugefuegtAm, zeitzone);
    }

    @Override
    public boolean equals(Object anderes) {
        return this == anderes
                || (anderes instanceof Merklisteneintrag eintrag && id.equals(eintrag.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
