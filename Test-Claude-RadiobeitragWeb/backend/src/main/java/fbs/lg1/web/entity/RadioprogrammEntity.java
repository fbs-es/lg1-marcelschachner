package fbs.lg1.web.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Datenbank-Abbild eines {@link fbs.lg1.Radioprogramm}s mit seinen Sendungen.
 */
@Entity
@Table(name = "radioprogramm")
public class RadioprogrammEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "programm", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startzeit")
    private final List<RadiosendungEntity> sendungen = new ArrayList<>();

    private LocalDate datum;

    /**
     * Wird nur von JPA verwendet.
     */
    protected RadioprogrammEntity() {
    }

    /**
     * Legt ein Programm-Abbild ohne Sendungen an.
     *
     * @param datum der Tag des Programms
     */
    public RadioprogrammEntity(LocalDate datum) {
        this.datum = datum;
    }

    /**
     * Nimmt ein Sendungs-Abbild an der von der Domäne bestimmten Position in das Programm auf.
     *
     * @param index die Position nach Startzeit
     * @param sendung die bereits von der Domäne geprüfte Sendung
     */
    public void sendungAufnehmen(int index, RadiosendungEntity sendung) {
        sendungen.add(index, sendung);
    }

    /**
     * @return die Datenbank-ID
     */
    public Long showId() {
        return id;
    }

    /**
     * @return der Tag des Programms
     */
    public LocalDate showDatum() {
        return datum;
    }

    /**
     * @return alle Sendungen nach Startzeit sortiert
     */
    public List<RadiosendungEntity> showSendungen() {
        return sendungen;
    }
}
