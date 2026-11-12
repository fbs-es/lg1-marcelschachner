package de.bestellsystem.katalog.persistence;

import de.bestellsystem.katalog.domain.Produkt;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProduktRepository extends JpaRepository<Produkt, UUID> {

    List<Produkt> findAllByOrderByTitelAsc();

    // Feste Sperrreihenfolge nach id verhindert Deadlocks zwischen Checkouts (ADR 0003).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produkt p where p.id in :ids order by p.id")
    List<Produkt> sperreZumEntnehmen(@Param("ids") Collection<UUID> ids);
}
