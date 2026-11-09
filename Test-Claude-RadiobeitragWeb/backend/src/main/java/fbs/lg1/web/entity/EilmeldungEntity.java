package fbs.lg1.web.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Datenbank-Abbild einer {@link fbs.lg1.Eilmeldung}.
 */
@Entity
@DiscriminatorValue("EILMELDUNG")
public class EilmeldungEntity extends RadiobeitragEntity {

    private int dringlichkeitsstufe;
    private String region;

    /**
     * Wird nur von JPA verwendet.
     */
    protected EilmeldungEntity() {
    }

    /**
     * Legt ein Abbild mit allen Daten an.
     */
    public EilmeldungEntity(String titel, int dauer, int dringlichkeitsstufe, String region) {
        super(titel, dauer);
        this.dringlichkeitsstufe = dringlichkeitsstufe;
        this.region = region;
    }

    /**
     * @return die Dringlichkeitsstufe von 1 bis 5
     */
    public int showDringlichkeitsstufe() {
        return dringlichkeitsstufe;
    }

    /**
     * @return die betroffene Region
     */
    public String showRegion() {
        return region;
    }
}
