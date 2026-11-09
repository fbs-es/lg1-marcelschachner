package fbs.lg1.web.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Datenbank-Abbild eines {@link fbs.lg1.Werbebeitrag}s.
 */
@Entity
@DiscriminatorValue("WERBUNG")
public class WerbebeitragEntity extends RadiobeitragEntity {

    private String kunde;
    private double werbebudget;

    /**
     * Wird nur von JPA verwendet.
     */
    protected WerbebeitragEntity() {
    }

    /**
     * Legt ein Abbild mit allen Daten an.
     */
    public WerbebeitragEntity(String titel, int dauer, String kunde, double werbebudget) {
        super(titel, dauer);
        this.kunde = kunde;
        this.werbebudget = werbebudget;
    }

    /**
     * @return der Name des Kunden
     */
    public String showKunde() {
        return kunde;
    }

    /**
     * @return das Werbebudget in Euro
     */
    public double showWerbebudget() {
        return werbebudget;
    }
}
