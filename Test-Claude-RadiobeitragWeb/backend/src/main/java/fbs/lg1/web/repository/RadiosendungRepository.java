package fbs.lg1.web.repository;

import fbs.lg1.web.entity.RadiosendungEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Datenbankzugriff auf gespeicherte {@link RadiosendungEntity}-Objekte.
 */
public interface RadiosendungRepository extends JpaRepository<RadiosendungEntity, Long> {

    /**
     * Sucht einen Datensatz über seine ID.
     *
     * @param id die Datenbank-ID
     * @return der gefundene Datensatz oder null, wenn es keinen gibt
     */
    RadiosendungEntity findOneById(Long id);
}
