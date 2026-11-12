package de.bestellsystem.versand.persistence;

import de.bestellsystem.versand.domain.Logistikdienstleister;
import de.bestellsystem.versand.domain.Versandpaket;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersandpaketRepository extends JpaRepository<Versandpaket, UUID> {

    /** I-11 über alle Bestellungen; der Unique-Constraint fängt gleichzeitiges Anlegen ab. */
    boolean existsByDienstleisterAndSendungsnummer(
            Logistikdienstleister dienstleister, String sendungsnummer);
}
