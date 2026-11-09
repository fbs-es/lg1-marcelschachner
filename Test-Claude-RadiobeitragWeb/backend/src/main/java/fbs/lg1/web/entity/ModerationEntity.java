package fbs.lg1.web.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Datenbank-Abbild einer {@link fbs.lg1.Moderation}.
 */
@Entity
@DiscriminatorValue("MODERATION")
public class ModerationEntity extends RadiobeitragEntity {

    private String sprechertext;

    /**
     * Wird nur von JPA verwendet.
     */
    protected ModerationEntity() {
    }

    /**
     * Legt ein Abbild mit allen Daten an.
     */
    public ModerationEntity(String titel, int dauer, String sprechertext) {
        super(titel, dauer);
        this.sprechertext = sprechertext;
    }

    /**
     * @return der Sprechertext
     */
    public String showSprechertext() {
        return sprechertext;
    }
}
