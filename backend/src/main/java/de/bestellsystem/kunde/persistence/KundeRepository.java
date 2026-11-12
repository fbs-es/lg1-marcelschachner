package de.bestellsystem.kunde.persistence;

import de.bestellsystem.kunde.domain.Email;
import de.bestellsystem.kunde.domain.Kunde;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KundeRepository extends JpaRepository<Kunde, UUID> {

    boolean existsByEmail(Email email);

    List<Kunde> findAllByOrderByNachnameAscVornameAsc();

    @Query("select k from Kunde k left join fetch k.adressen where k.id = :id")
    Optional<Kunde> findeMitAdressen(@Param("id") UUID id);
}
