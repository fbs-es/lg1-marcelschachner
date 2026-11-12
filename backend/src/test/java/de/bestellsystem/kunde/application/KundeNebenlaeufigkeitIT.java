package de.bestellsystem.kunde.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.kunde.domain.Adresse;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.kunde.persistence.KundeRepository;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersKonfiguration.class)
class KundeNebenlaeufigkeitIT {

    private static final Anschrift BERLIN = new Anschrift("Hauptstraße 1", "Berlin", "10115", "DE");
    private static final Anschrift WIEN = new Anschrift("Ring 2", "Wien", "1010", "AT");
    private static final long WARTEZEIT_SEKUNDEN = 10;

    @Autowired private KundeService service;
    @Autowired private KundeRepository repository;
    @Autowired private TransactionTemplate transaktion;
    @Autowired private JdbcClient jdbc;

    @BeforeEach
    void leereKunden() {
        repository.deleteAll();
    }

    @Test
    @DisplayName(
            "US-01.3 AK8: zwei gleichzeitige Löschungen der beiden Lieferadressen, eine gewinnt")
    void gleichzeitigesLoeschen() throws Exception {
        Kunde kunde = service.registriere("Anna", "Muster", "anna@example.de", BERLIN, false);
        UUID erste = kunde.getAdressen().getFirst().getId();
        UUID zweite = service.fuegeAdresseHinzu(kunde.getId(), WIEN, true, false).getId();
        CountDownLatch ersteGeladen = new CountDownLatch(1);
        CountDownLatch zweiteGeladen = new CountDownLatch(1);

        // Beide Transaktionen lesen denselben Stand, bevor die erste festschreibt.
        CompletableFuture<Void> gewinner =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            service.entferneAdresse(kunde.getId(), erste);
                            ersteGeladen.countDown();
                            warte(zweiteGeladen);
                        }));
        CompletableFuture<Void> verlierer =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            warte(ersteGeladen);
                            service.entferneAdresse(kunde.getId(), zweite);
                            zweiteGeladen.countDown();
                            gewinner.join();
                        }));

        gewinner.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS);
        assertThatThrownBy(() -> verlierer.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(service.finde(kunde.getId()).getAdressen())
                .extracting(Adresse::getId)
                .containsExactly(zweite);
    }

    @Test
    @DisplayName("US-01.3 AK8: gleichzeitiges Entziehen des Lieferkennzeichens, eine gewinnt")
    void gleichzeitigesAendern() throws Exception {
        Kunde kunde = service.registriere("Anna", "Muster", "anna@example.de", BERLIN, false);
        UUID erste = kunde.getAdressen().getFirst().getId();
        UUID zweite = service.fuegeAdresseHinzu(kunde.getId(), WIEN, true, false).getId();
        CountDownLatch ersteGeladen = new CountDownLatch(1);
        CountDownLatch zweiteGeladen = new CountDownLatch(1);

        CompletableFuture<Void> gewinner =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            service.aendereAdresse(kunde.getId(), erste, BERLIN, false, true);
                            ersteGeladen.countDown();
                            warte(zweiteGeladen);
                        }));
        CompletableFuture<Void> verlierer =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            warte(ersteGeladen);
                            service.aendereAdresse(kunde.getId(), zweite, WIEN, false, true);
                            zweiteGeladen.countDown();
                            gewinner.join();
                        }));

        gewinner.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS);
        assertThatThrownBy(() -> verlierer.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS))
                .hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(service.finde(kunde.getId()).getAdressen())
                .filteredOn(Adresse::istLieferadresse)
                .extracting(Adresse::getId)
                .containsExactly(zweite);
    }

    @Test
    @DisplayName("D-16: gleichzeitige Registrierung derselben E-Mail, die zweite ergibt Konflikt")
    void gleichzeitigeRegistrierung() throws Exception {
        CountDownLatch ersteEingefuegt = new CountDownLatch(1);
        CountDownLatch zweiteWartet = new CountDownLatch(1);

        CompletableFuture<Void> erste =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            service.registriere("Anna", "Muster", "anna@example.de", BERLIN, false);
                            ersteEingefuegt.countDown();
                            warte(zweiteWartet);
                        }));
        warte(ersteEingefuegt);
        // Die zweite sieht die noch nicht festgeschriebene erste nicht und blockiert am Index.
        CompletableFuture<Void> zweite =
                CompletableFuture.runAsync(() ->
                        service.registriere("Anna", "Muster", " Anna@Example.DE ", BERLIN, false));
        warteBisBlockiert();
        zweiteWartet.countDown();

        erste.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS);
        assertThatThrownBy(() -> zweite.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS))
                .hasCauseInstanceOf(KonfliktException.class);
        assertThat(repository.count()).isEqualTo(1);
    }

    private void warteBisBlockiert() throws InterruptedException {
        long ende = System.nanoTime() + Duration.ofSeconds(WARTEZEIT_SEKUNDEN).toNanos();
        while (System.nanoTime() < ende) {
            int blockiert =
                    jdbc.sql(
                                    "SELECT count(*) FROM pg_stat_activity"
                                            + " WHERE wait_event_type = 'Lock'"
                                            + " AND query LIKE 'insert into kunde%'")
                            .query(Integer.class)
                            .single();
            if (blockiert > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Zweite Registrierung blockiert nicht am Unique-Index");
    }

    private static void warte(CountDownLatch signal) {
        try {
            if (!signal.await(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS)) {
                throw new AssertionError(
                        "Zeitüberschreitung beim Warten auf die andere Transaktion");
            }
        } catch (InterruptedException unterbrochen) {
            Thread.currentThread().interrupt();
            throw new AssertionError(unterbrochen);
        }
    }
}
