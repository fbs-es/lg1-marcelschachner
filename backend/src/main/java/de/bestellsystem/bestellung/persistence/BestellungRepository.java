package de.bestellsystem.bestellung.persistence;

import de.bestellsystem.bestellung.domain.Bestellung;
import java.time.LocalDate;
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

    /** Zahlungen aller Bestellungen des Kunden in denselben Persistenzkontext (US-04.2, D-19). */
    @Query("select b from Bestellung b join fetch b.zahlungen where b.kundeId = :kundeId")
    List<Bestellung> findeZahlungenDesKunden(@Param("kundeId") UUID kundeId);

    @Query(
            """
            select b from Bestellung b
              join fetch b.zahlungen
            where b.bestellnummer = :bestellnummer and b.kundeId = :kundeId
            """)
    Optional<Bestellung> findeMitZahlungen(
            @Param("kundeId") UUID kundeId, @Param("bestellnummer") String bestellnummer);

    // Drei Abfragen statt einer (MultipleBagFetchException), alle in denselben Persistenzkontext.
    @Query(
            """
            select b from Bestellung b
              join fetch b.positionen
            where b.kundeId = :kundeId and b.faelligAm < :stichtag
            """)
    List<Bestellung> findeFaelligeMitPositionen(
            @Param("kundeId") UUID kundeId, @Param("stichtag") LocalDate stichtag);

    @Query(
            """
            select b from Bestellung b
              join fetch b.zahlungen
            where b.kundeId = :kundeId and b.faelligAm < :stichtag
            """)
    List<Bestellung> findeZahlungenFaelliger(
            @Param("kundeId") UUID kundeId, @Param("stichtag") LocalDate stichtag);

    @Query(
            """
            select b from Bestellung b
              left join fetch b.mahnungen
            where b.kundeId = :kundeId and b.faelligAm < :stichtag
            """)
    List<Bestellung> findeMahnungenFaelliger(
            @Param("kundeId") UUID kundeId, @Param("stichtag") LocalDate stichtag);

    /** Kandidaten des Mahnlaufs: Kunden mit mindestens einer Bestellung fällig vor dem Stichtag. */
    @Query(
            "select distinct b.kundeId from Bestellung b where b.faelligAm < :stichtag"
                    + " order by b.kundeId")
    List<UUID> findeKundenMitFaelligenBestellungen(@Param("stichtag") LocalDate stichtag);
}
