package de.bestellsystem.gutschein.persistence;

import de.bestellsystem.gutschein.domain.Gutschein;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GutscheinRepository extends JpaRepository<Gutschein, UUID> {

    /** Erwartet einen mit {@link Gutschein#normalisiere} normalisierten Code. */
    Optional<Gutschein> findByCode(String code);
}
