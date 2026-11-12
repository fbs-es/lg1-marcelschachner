package de.bestellsystem.bestellung.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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

/** Zwei gleichzeitige Checkouts auf dasselbe Produkt (D-09, ADR 0003). */
@SpringBootTest
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class BestellungNebenlaeufigkeitIT {

    private static final Anschrift BERLIN = new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");
    private static final long WARTEZEIT_SEKUNDEN = 10;

    @Autowired private BestellungService service;
    @Autowired private KundeService kundeService;
    @Autowired private ProduktRepository produkte;
    @Autowired private TransactionTemplate transaktion;
    @Autowired private JdbcClient jdbc;

    private UUID kundenId;
    private UUID lieferadresse;

    @BeforeEach
    void kunde() {
        Datenbestand.leere(jdbc);
        Kunde kunde = kundeService.registriere("Anna", "Muster", "anna@example.de", BERLIN, false);
        kundenId = kunde.getId();
        lieferadresse = kunde.getAdressen().getFirst().getId();
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("Fahrplan 4a: zwei Checkouts auf die letzte Lampe, genau einer erfolgreich")
    void letzteLampe() throws Exception {
        Produkt lampe = produkte.save(new Produkt("Lampe", Geld.ausCent(3000), 1));

        CompletableFuture<Void> zweiter = gleichzeitigMitErstem(lampe);

        assertThatThrownBy(() -> zweiter.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS))
                .hasCauseInstanceOf(KonfliktException.class)
                .hasMessageContaining("Lampe");
        assertThat(anzahlBestellungen()).isEqualTo(1);
        assertThat(produkte.findById(lampe.getId()).orElseThrow().getLagerbestand()).isZero();
    }

    @Test
    @DisplayName("ADR 0003: zwei gleichzeitige Checkouts mit genug Bestand gelingen beide")
    void genugBestand() throws Exception {
        Produkt buch = produkte.save(new Produkt("Buch", Geld.ausCent(1250), 10));

        gleichzeitigMitErstem(buch).get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS);

        assertThat(anzahlBestellungen()).isEqualTo(2);
        assertThat(produkte.findById(buch.getId()).orElseThrow().getLagerbestand()).isEqualTo(8);
    }

    /**
     * Der erste Checkout hält die Sperre, bis der zweite nachweislich an ihr wartet; danach
     * schreibt der erste fest. Liefert den zweiten Checkout.
     */
    private CompletableFuture<Void> gleichzeitigMitErstem(Produkt produkt) throws Exception {
        CountDownLatch ersterHaeltSperre = new CountDownLatch(1);
        CountDownLatch zweiterWartet = new CountDownLatch(1);

        CompletableFuture<Void> erster =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            bestelle(produkt);
                            ersterHaeltSperre.countDown();
                            warte(zweiterWartet);
                        }));
        warte(ersterHaeltSperre);
        CompletableFuture<Void> zweiter = CompletableFuture.runAsync(() -> bestelle(produkt));
        warteBisBlockiert();
        zweiterWartet.countDown();

        erster.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS);
        return zweiter;
    }

    private void bestelle(Produkt produkt) {
        service.gibAuf(
                kundenId,
                List.of(new Positionswunsch(produkt.getId(), 1)),
                lieferadresse,
                Zahlungsart.RECHNUNG,
                null);
    }

    private long anzahlBestellungen() {
        return jdbc.sql("SELECT count(*) FROM bestellung").query(Long.class).single();
    }

    private void warteBisBlockiert() throws InterruptedException {
        long ende = System.nanoTime() + Duration.ofSeconds(WARTEZEIT_SEKUNDEN).toNanos();
        while (System.nanoTime() < ende) {
            int blockiert =
                    jdbc.sql(
                                    "SELECT count(*) FROM pg_stat_activity"
                                            + " WHERE wait_event_type = 'Lock'"
                                            + " AND query LIKE '%from produkt%for%update%'")
                            .query(Integer.class)
                            .single();
            if (blockiert > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Zweiter Checkout wartet nicht an der Produktsperre");
    }

    private static void warte(CountDownLatch signal) {
        try {
            if (!signal.await(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS)) {
                throw new AssertionError("Zeitüberschreitung beim Warten auf den anderen Checkout");
            }
        } catch (InterruptedException unterbrochen) {
            Thread.currentThread().interrupt();
            throw new AssertionError(unterbrochen);
        }
    }
}
