package fbs.lg1.web.repository;

import fbs.lg1.web.entity.RadioprogrammEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Datenbankzugriff auf gespeicherte {@link RadioprogrammEntity}-Objekte.
 */
public interface RadioprogrammRepository extends JpaRepository<RadioprogrammEntity, Long> {

    /**
     * Sucht einen Datensatz über seine ID.
     *
     * @param id die Datenbank-ID
     * @return der gefundene Datensatz oder null, wenn es keinen gibt
     */
    RadioprogrammEntity findOneById(Long id);
}
