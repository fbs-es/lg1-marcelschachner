package de.bestellsystem.mahnwesen.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Protokoll einer Mahnung je Bestellung (D-03); Datum und Betrag sind eingefroren (D-20). */
@Entity
@Table(name = "mahnung")
public class Mahnung {

    @Id private UUID id;

    private LocalDate erstelltAm;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "gemahnter_betrag_cent"))
    private Geld gemahnterBetrag;

    protected Mahnung() {}

    public Mahnung(LocalDate erstelltAm, Geld gemahnterBetrag) {
        if (erstelltAm == null) {
            throw new IllegalArgumentException("erstelltAm ist erforderlich");
        }
        if (gemahnterBetrag == null || gemahnterBetrag.compareTo(Geld.NULL) <= 0) {
            throw new IllegalArgumentException("gemahnterBetrag muss größer 0 sein");
        }
        this.id = UUID.randomUUID();
        this.erstelltAm = erstelltAm;
        this.gemahnterBetrag = gemahnterBetrag;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getErstelltAm() {
        return erstelltAm;
    }

    public Geld getGemahnterBetrag() {
        return gemahnterBetrag;
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes || (anderes instanceof Mahnung mahnung && id.equals(mahnung.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
