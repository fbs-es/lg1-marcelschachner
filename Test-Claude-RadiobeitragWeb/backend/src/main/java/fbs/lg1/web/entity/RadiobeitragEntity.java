package fbs.lg1.web.entity;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Abstraktes Datenbank-Abbild eines {@link fbs.lg1.Radiobeitrag}s. Alle Beitragsarten liegen in
 * einer gemeinsamen Tabelle ({@code SINGLE_TABLE}), die Spalte {@code typ} unterscheidet sie.
 */
@Entity
@Table(name = "radiobeitrag")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "typ")
public abstract class RadiobeitragEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titel;
    private int dauer;

    /**
     * Wird nur von JPA verwendet.
     */
    protected RadiobeitragEntity() {
    }

    /**
     * Legt ein Beitrags-Abbild an.
     *
     * @param titel der Titel des Beitrags
     * @param dauer die Dauer in Sekunden
     */
    protected RadiobeitragEntity(String titel, int dauer) {
        this.titel = titel;
        this.dauer = dauer;
    }

    /**
     * Übernimmt die von der Domäne geänderte Dauer.
     *
     * @param neueDauer die neue Dauer in Sekunden
     */
    public void aendereDauer(int neueDauer) {
        dauer = neueDauer;
    }

    /**
     * @return die Datenbank-ID
     */
    public Long showId() {
        return id;
    }

    /**
     * @return der Titel des Beitrags
     */
    public String showTitel() {
        return titel;
    }

    /**
     * @return die Dauer in Sekunden
     */
    public int showDauer() {
        return dauer;
    }
}
