package fbs.lg1.web.repository;

import fbs.lg1.web.entity.RadiosenderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Datenbankzugriff auf gespeicherte {@link RadiosenderEntity}-Objekte.
 */
public interface RadiosenderRepository extends JpaRepository<RadiosenderEntity, Long> {

    /**
     * Sucht einen Datensatz über seine ID.
     *
     * @param id die Datenbank-ID
     * @return der gefundene Datensatz oder null, wenn es keinen gibt
     */
    RadiosenderEntity findOneById(Long id);
}
