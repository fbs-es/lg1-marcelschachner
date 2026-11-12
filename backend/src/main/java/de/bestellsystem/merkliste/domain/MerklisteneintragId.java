package de.bestellsystem.merkliste.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

/** Ein Produkt steht höchstens einmal auf der Merkliste eines Kunden (I-4). */
@Embeddable
public record MerklisteneintragId(
        @Column(name = "kunde_id") UUID kundeId, @Column(name = "produkt_id") UUID produktId) {

    public MerklisteneintragId {
        Objects.requireNonNull(kundeId, "kundeId ist erforderlich");
        Objects.requireNonNull(produktId, "produktId ist erforderlich");
    }
}
