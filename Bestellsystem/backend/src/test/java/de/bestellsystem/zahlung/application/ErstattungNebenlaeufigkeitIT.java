package de.bestellsystem.zahlung.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.bestellung.application.Positionswunsch;
import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersKonfiguration.class)
class ErstattungNebenlaeufigkeitIT {

    private static final long WARTEZEIT_SEKUNDEN = 10;
    private static final Geld SECHZIG = Geld.ausCent(6_000);

    @Autowired private BestellungService service;
    @Autowired private KundeService kundeService;
    @Autowired private ProduktRepository produkte;
    @Autowired private TransactionTemplate transaktion;
    @Autowired private JdbcClient jdbc;

    private UUID kundenId;
    private String bestellnummer;

    @BeforeEach
    void bezahlteBestellungUeber100Euro() {
        Datenbestand.leere(jdbc);
        Produkt artikel = produkte.save(new Produkt("Artikel", Geld.ausCent(10_000), 1));
        Kunde kunde =
                kundeService.registriere(
                        "Anna",
                        "Muster",
                        "anna@example.de",
                        new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                        false);
        kundenId = kunde.getId();
        Bestellung bestellung =
                service.gibAuf(
                        kundenId,
                        List.of(new Positionswunsch(artikel.getId(), 1)),
                        kunde.getAdressen().getFirst().getId(),
                        Zahlungsart.RECHNUNG,
                        null);
        bestellnummer = bestellung.getBestellnummer();
        service.aendereZahlungsstatus(
                kundenId,
                bestellnummer,
                bestellung.getZahlungen().getFirst().getId(),
                Transaktionsstatus.ERFOLGREICH);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("D-19: zwei gleichzeitige Erstattungen über je 60,00 €, nur eine gelingt")
    void gleichzeitigeErstattungen() throws Exception {
        CountDownLatch ersteGebucht = new CountDownLatch(1);
        CountDownLatch zweiteGebucht = new CountDownLatch(1);

        // Beide sehen 100,00 € erstattbar, bevor die erste festschreibt.
        CompletableFuture<Void> gewinner =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            service.bucheErstattung(
                                    kundenId, bestellnummer, Zahlungsart.RECHNUNG, SECHZIG);
                            ersteGebucht.countDown();
                            warte(zweiteGebucht);
                        }));
        CompletableFuture<Void> verlierer =
                CompletableFuture.runAsync(() ->
                        transaktion.executeWithoutResult(status -> {
                            warte(ersteGebucht);
                            service.bucheErstattung(
                                    kundenId, bestellnummer, Zahlungsart.RECHNUNG, SECHZIG);
                            zweiteGebucht.countDown();
                            gewinner.join();
                        }));

        gewinner.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS);
        assertThatThrownBy(() -> verlierer.get(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(service.finde(kundenId, bestellnummer).getZahlungen())
                .filteredOn(zahlung -> zahlung.getBuchungsart() == Buchungsart.ERSTATTUNG)
                .extracting(Zahlung::getBetrag)
                .containsExactly(SECHZIG);
    }

    private static void warte(CountDownLatch signal) {
        try {
            if (!signal.await(WARTEZEIT_SEKUNDEN, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Zeitüberschreitung beim Warten");
            }
        } catch (InterruptedException unterbrochen) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(unterbrochen);
        }
    }
}
