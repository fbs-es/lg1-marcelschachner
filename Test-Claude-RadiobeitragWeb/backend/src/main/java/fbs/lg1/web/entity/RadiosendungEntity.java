package fbs.lg1.web.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Datenbank-Abbild einer {@link fbs.lg1.Radiosendung}. Die Reihenfolge des Sendeplans wird in der
 * Spalte {@code position_nr} gespeichert.
 */
@Entity
@Table(name = "radiosendung")
public class RadiosendungEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "programm_id")
    private RadioprogrammEntity programm;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sendung_id")
    @OrderColumn(name = "position_nr")
    private final List<RadiobeitragEntity> beitraege = new ArrayList<>();

    private String name;
    private String moderator;
    private LocalTime startzeit;
    private LocalTime endzeit;
    private int maximaldauer;

    /**
     * Wird nur von JPA verwendet.
     */
    protected RadiosendungEntity() {
    }

    /**
     * Legt ein Sendungs-Abbild ohne Beiträge an.
     *
     * @param programm das Programm, zu dem die Sendung gehört
     * @param name der Name der Sendung
     * @param moderator der Name des Moderators
     * @param startzeit der Beginn des Zeitblocks
     * @param endzeit das Ende des Zeitblocks
     * @param maximaldauer die erlaubte Sendezeit in Sekunden
     */
    public RadiosendungEntity(
            RadioprogrammEntity programm,
            String name,
            String moderator,
            LocalTime startzeit,
            LocalTime endzeit,
            int maximaldauer) {
        this.programm = programm;
        this.name = name;
        this.moderator = moderator;
        this.startzeit = startzeit;
        this.endzeit = endzeit;
        this.maximaldauer = maximaldauer;
    }

    /**
     * Ersetzt den gespeicherten Sendeplan durch die neue Reihenfolge aus der Domäne.
     *
     * @param neueBeitraege die Beiträge in der neuen Reihenfolge
     */
    public void beitraegeErsetzen(List<RadiobeitragEntity> neueBeitraege) {
        beitraege.clear();
        beitraege.addAll(neueBeitraege);
    }

    /**
     * @return die Datenbank-ID
     */
    public Long showId() {
        return id;
    }

    /**
     * @return das Programm, zu dem die Sendung gehört
     */
    public RadioprogrammEntity showProgramm() {
        return programm;
    }

    /**
     * @return der Name der Sendung
     */
    public String showName() {
        return name;
    }

    /**
     * @return der Name des Moderators
     */
    public String showModerator() {
        return moderator;
    }

    /**
     * @return der Beginn des Zeitblocks
     */
    public LocalTime showStartzeit() {
        return startzeit;
    }

    /**
     * @return das Ende des Zeitblocks
     */
    public LocalTime showEndzeit() {
        return endzeit;
    }

    /**
     * @return die erlaubte Sendezeit in Sekunden
     */
    public int showMaximaldauer() {
        return maximaldauer;
    }

    /**
     * @return die Beiträge in Sendeplan-Reihenfolge
     */
    public List<RadiobeitragEntity> showBeitraege() {
        return beitraege;
    }
}
