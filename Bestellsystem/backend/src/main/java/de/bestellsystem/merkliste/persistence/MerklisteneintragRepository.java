package de.bestellsystem.merkliste.persistence;

import de.bestellsystem.merkliste.domain.Merklisteneintrag;
import de.bestellsystem.merkliste.domain.MerklisteneintragId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerklisteneintragRepository
        extends JpaRepository<Merklisteneintrag, MerklisteneintragId> {

    /**
     * Legt den Eintrag an, falls er fehlt; liefert 1 für neu und 0 für schon gemerkt. ON CONFLICT
     * statt Lesen-dann-Schreiben, damit auch gleichzeitiges Merken das erste Datum behält (D-21).
     */
    @Modifying
    @Query(
            value =
                    "INSERT INTO merklisteneintrag (kunde_id, produkt_id, hinzugefuegt_am)"
                            + " VALUES (:kundeId, :produktId, :zeitpunkt)"
                            + " ON CONFLICT (kunde_id, produkt_id) DO NOTHING",
            nativeQuery = true)
    int merke(
            @Param("kundeId") UUID kundeId,
            @Param("produktId") UUID produktId,
            @Param("zeitpunkt") Instant zeitpunkt);

    @Query(
            "select e from Merklisteneintrag e join fetch e.produkt"
                    + " where e.id.kundeId = :kundeId"
                    + " order by e.hinzugefuegtAm desc, e.produkt.titel asc")
    List<Merklisteneintrag> findeMerkliste(@Param("kundeId") UUID kundeId);

    @Query("select e from Merklisteneintrag e join fetch e.produkt where e.id = :id")
    Optional<Merklisteneintrag> findeEintrag(@Param("id") MerklisteneintragId id);

    @Modifying
    @Query("delete from Merklisteneintrag e where e.id = :id")
    void entferne(@Param("id") MerklisteneintragId id);
}
