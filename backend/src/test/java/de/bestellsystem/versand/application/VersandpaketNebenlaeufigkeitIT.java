package de.bestellsystem.versand.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.bestellung.application.Positionswunsch;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.versand.domain.Logistikdienstleister;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersKonfiguration.class)
class VersandpaketNebenlaeufigkeitIT {

    private static final long WARTEZEIT_SEKUNDEN = 10;
    private static final String NUMMER = "AB123DE";

    @Autowired private BestellungService service;
    @Autowired private KundeService kundeService;
    @Autowired private ProduktRepository produkte;
    @Autowired private TransactionTemplate transaktion;
    @Autowired private JdbcClient jdbc;

    private UUID kundenId;
    private String ersteBestellung;
    private String zweiteBestellung;

    @BeforeEach
    void zweiBestellungen() {
        Datenbestand.leere(jdbc);
        Produkt artikel = produkte.save(new Produkt("Artikel", Geld.ausCent(1_000), 10));
        Kunde kunde =
                kundeService.registriere(
                        "Anna",
                        "Muster",
                        "anna@example.de",
                        new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                        false);
        kundenId = kunde.getId();
        UUID lieferadresse = kunde.getAdressen().getFirst().getId();
        ersteBestellung = bestelle(artikel, lieferadresse);
        zweiteBestellung = bestelle(artikel, lieferadresse);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("US-07.1 AK5, I-11: gleiche Sendungsnummer gleichzeitig, genau eine gelingt (409)")
    void gleicheSendungsnummerGleichzeitig() throws Exception {
        CountDownLatch ersterHatEingefuegt = new CountDownLatch(1);
        CountDownLatch zweiterWartet = new CountDownLatch(1);

        CompletableFuture<Void> erster =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            legeAn(ersteBestellung);
                            ersterHatEingefuegt.countDown();
                            warte(zweiterWartet);
                        }));
        warte(ersterHatEingefuegt);
        CompletableFuture<Void> zweiter =
                CompletableFuture.runAsync(() -> legeAn(zweiteBestellung));
        warteBisBlockiert();
        zweiterWartet.countDown();

        erster.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS);
        assertThatThrownBy(() -> zweiter.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(KonfliktException.class);
        assertThat(jdbc.sql("SELECT count(*) FROM versandpaket").query(Long.class).single())
                .isEqualTo(1);
    }

    private void legeAn(String bestellnummer) {
        service.legePaketAn(kundenId, bestellnummer, Logistikdienstleister.DHL, NUMMER);
    }

    private String bestelle(Produkt artikel, UUID lieferadresse) {
        return service.gibAuf(
                        kundenId,
                        List.of(new Positionswunsch(artikel.getId(), 1)),
                        lieferadresse,
                        Zahlungsart.RECHNUNG,
                        null)
                .getBestellnummer();
    }

    // Der zweite hat die Prüfung auf vorhandene Nummern bestanden und wartet am Unique-Index.
    private void warteBisBlockiert() throws InterruptedException {
        long ende = System.nanoTime() + Duration.ofSeconds(WARTEZEIT_SEKUNDEN).toNanos();
        while (System.nanoTime() < ende) {
            int blockiert =
                    jdbc.sql(
                                    "SELECT count(*) FROM pg_stat_activity"
                                            + " WHERE wait_event_type = 'Lock'"
                                            + " AND query LIKE '%insert into versandpaket%'")
                            .query(Integer.class)
                            .single();
            if (blockiert > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Zweites Anlegen wartet nicht am Unique-Index");
    }

    private static void warte(CountDownLatch signal) {
        try {
            if (!signal.await(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS)) {
                throw new AssertionError("Zeitüberschreitung beim Warten auf das andere Anlegen");
            }
        } catch (InterruptedException unterbrochen) {
            Thread.currentThread().interrupt();
            throw new AssertionError(unterbrochen);
        }
    }
}
