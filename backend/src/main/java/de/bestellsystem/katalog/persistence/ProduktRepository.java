package de.bestellsystem.katalog.persistence;

import de.bestellsystem.katalog.domain.Produkt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduktRepository extends JpaRepository<Produkt, UUID> {

    List<Produkt> findAllByOrderByTitelAsc();
}
