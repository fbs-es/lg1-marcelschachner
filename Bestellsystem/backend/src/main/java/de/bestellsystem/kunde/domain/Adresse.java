package de.bestellsystem.kunde.domain;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

/** Eintrag im Adressbuch eines Kunden; wird nur über {@link Kunde} verändert (D-06). */
@Entity
@Table(name = "adresse")
public class Adresse {

    @Id private UUID id;

    @Embedded private Anschrift anschrift;

    private boolean istLieferadresse;

    private boolean istRechnungsadresse;

    protected Adresse() {}

    Adresse(Anschrift anschrift, boolean istLieferadresse, boolean istRechnungsadresse) {
        this.id = UUID.randomUUID();
        aendere(anschrift, istLieferadresse, istRechnungsadresse);
    }

    void aendere(Anschrift anschrift, boolean istLieferadresse, boolean istRechnungsadresse) {
        pruefe(anschrift, istLieferadresse, istRechnungsadresse);
        this.anschrift = anschrift;
        this.istLieferadresse = istLieferadresse;
        this.istRechnungsadresse = istRechnungsadresse;
    }

    static void pruefe(Anschrift anschrift, boolean istLieferadresse, boolean istRechnungsadresse) {
        if (anschrift == null) {
            throw new IllegalArgumentException("anschrift ist erforderlich");
        }
        if (!istLieferadresse && !istRechnungsadresse) {
            throw new IllegalArgumentException(
                    "Eine Adresse muss Liefer- oder Rechnungsadresse sein (I-2)");
        }
    }

    public UUID getId() {
        return id;
    }

    public Anschrift getAnschrift() {
        return anschrift;
    }

    public boolean istLieferadresse() {
        return istLieferadresse;
    }

    public boolean istRechnungsadresse() {
        return istRechnungsadresse;
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes || (anderes instanceof Adresse adresse && id.equals(adresse.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
