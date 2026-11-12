package de.bestellsystem.bestellung.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

/** Zeile einer Bestellung mit eingefrorenem Einzelpreis (D-10, I-8). */
@Entity
@Table(name = "bestellposition")
public class Bestellposition {

    @Id private UUID id;

    private int zeilennummer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produkt_id")
    private Produkt produkt;

    private int menge;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "einzelpreis_cent"))
    private Geld einzelpreis;

    protected Bestellposition() {}

    Bestellposition(int zeilennummer, Produkt produkt, int menge) {
        this.id = UUID.randomUUID();
        this.zeilennummer = zeilennummer;
        this.produkt = produkt;
        this.menge = menge;
        this.einzelpreis = produkt.getListenpreis();
    }

    public Geld berechneZwischenbetrag() {
        return einzelpreis.multipliziere(menge);
    }

    public int getZeilennummer() {
        return zeilennummer;
    }

    public Produkt getProdukt() {
        return produkt;
    }

    public int getMenge() {
        return menge;
    }

    public Geld getEinzelpreis() {
        return einzelpreis;
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes
                || (anderes instanceof Bestellposition position && id.equals(position.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
