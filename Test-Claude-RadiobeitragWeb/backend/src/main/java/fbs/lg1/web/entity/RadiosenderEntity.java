package fbs.lg1.web.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Datenbank-Abbild eines {@link fbs.lg1.Radiosender}s mit genau einem Programm.
 */
@Entity
@Table(name = "radiosender")
public class RadiosenderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "programm_id", unique = true)
    private RadioprogrammEntity programm;

    private String name;
    private double frequenz;

    /**
     * Wird nur von JPA verwendet.
     */
    protected RadiosenderEntity() {
    }

    /**
     * Legt ein Sender-Abbild an.
     *
     * @param name der Name des Senders
     * @param frequenz die Übertragungsfrequenz in MHz
     * @param programm das Programm des Senders
     */
    public RadiosenderEntity(String name, double frequenz, RadioprogrammEntity programm) {
        this.name = name;
        this.frequenz = frequenz;
        this.programm = programm;
    }

    /**
     * @return die Datenbank-ID
     */
    public Long showId() {
        return id;
    }

    /**
     * @return der Name des Senders
     */
    public String showName() {
        return name;
    }

    /**
     * @return die Übertragungsfrequenz in MHz
     */
    public double showFrequenz() {
        return frequenz;
    }

    /**
     * @return das Programm des Senders
     */
    public RadioprogrammEntity showProgramm() {
        return programm;
    }
}
