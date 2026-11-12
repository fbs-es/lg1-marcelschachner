package de.bestellsystem.bestellung.persistence;

import de.bestellsystem.bestellung.domain.Bestellung;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BestellungRepository extends JpaRepository<Bestellung, String> {

    // Zwei Listen lassen sich nicht in einer Abfrage holen (MultipleBagFetchException);
    // die Zahlungen lädt findeZahlungen in denselben Persistenzkontext nach.
    @Query(
            """
            select b from Bestellung b
              join fetch b.positionen p
              join fetch p.produkt
              left join fetch b.gutschein
            where b.bestellnummer = :bestellnummer and b.kundeId = :kundeId
            """)
    Optional<Bestellung> findeMitPositionen(
            @Param("kundeId") UUID kundeId, @Param("bestellnummer") String bestellnummer);

    @Query(
            "select b from Bestellung b join fetch b.zahlungen where b.bestellnummer ="
                    + " :bestellnummer")
    Optional<Bestellung> findeZahlungen(@Param("bestellnummer") String bestellnummer);
}
