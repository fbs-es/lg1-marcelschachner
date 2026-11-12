package de.bestellsystem.bestellung.persistence;

import de.bestellsystem.bestellung.domain.Bestellung;
import java.util.List;
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

    // Der Zähler der Bestellnummer ist mindestens sechsstellig, kann aber länger werden (D-14);
    // erst nach Länge sortieren, damit B-2026-1000000 nach B-2026-999999 kommt.
    @Query(
            """
            select b from Bestellung b
              join fetch b.positionen
            where b.kundeId = :kundeId
            order by b.bestelldatum desc, length(b.bestellnummer) desc, b.bestellnummer desc
            """)
    List<Bestellung> findeHistorie(@Param("kundeId") UUID kundeId);
}
