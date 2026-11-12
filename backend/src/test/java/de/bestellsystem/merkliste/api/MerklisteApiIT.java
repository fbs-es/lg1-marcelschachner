package de.bestellsystem.merkliste.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.VerstellbareUhrKonfiguration;
import de.bestellsystem.VerstellbareUhrKonfiguration.VerstellbareUhr;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** US-03.1 und US-03.2 über HTTP (D-07, D-21); die Uhr wird je Test auf den Merktag gestellt. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, VerstellbareUhrKonfiguration.class})
class MerklisteApiIT {

    private static final String MERKLISTE = "/api/v1/kunden/{kundenId}/merkliste";
    private static final String EINTRAG = MERKLISTE + "/{produktId}";
    private static final Anschrift BERLIN = new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");

    private static final Instant AM_12_11 = Instant.parse("2026-11-12T09:30:00Z");
    private static final Instant AM_15_11 = Instant.parse("2026-11-15T10:00:00Z");
    private static final Instant AM_20_11 = Instant.parse("2026-11-20T08:00:00Z");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcClient jdbc;
    @Autowired private ProduktRepository produkte;
    @Autowired private KundeService kundeService;
    @Autowired private VerstellbareUhr uhr;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private UUID kopfhoerer;
    private UUID kabel;
    private UUID ich;
    private UUID andererKunde;

    @BeforeEach
    void beispieldaten() {
        Datenbestand.leere(jdbc);
        kopfhoerer = produkte.save(new Produkt("Kopfhörer", Geld.ausCent(7999), 3)).getId();
        kabel = produkte.save(new Produkt("Kabel", Geld.ausCent(999), 0)).getId();
        ich = kundeService.registriere("Anna", "Muster", "anna@example.de", BERLIN, false).getId();
        andererKunde =
                kundeService
                        .registriere("Ben", "Beispiel", "ben@example.de", BERLIN, false)
                        .getId();
        uhr.stelle(AM_12_11);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("US-03.1 AK1: Produkt merken legt Eintrag mit heutigem Datum an (201 + Location)")
    void produktMerken() throws Exception {
        merke(ich, kopfhoerer)
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        endsWith(
                                                "/api/v1/kunden/"
                                                        + ich
                                                        + "/merkliste/"
                                                        + kopfhoerer)))
                .andExpect(jsonPath("$.produktId").value(kopfhoerer.toString()))
                .andExpect(jsonPath("$.titel").value("Kopfhörer"))
                .andExpect(jsonPath("$.hinzugefuegtAm").value("2026-11-12"));

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titel").value("Kopfhörer"))
                .andExpect(jsonPath("$[0].hinzugefuegtAm").value("2026-11-12"));
        mockMvc.perform(get(EINTRAG, ich, kopfhoerer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hinzugefuegtAm").value("2026-11-12"));
    }

    @Test
    @DisplayName("US-03.1 AK2: doppeltes Merken ändert nichts, Datum bleibt 12.11.2026 (200)")
    void doppeltesMerken() throws Exception {
        merke(ich, kopfhoerer).andExpect(status().isCreated());

        uhr.stelle(AM_20_11);
        merke(ich, kopfhoerer)
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.hinzugefuegtAm").value("2026-11-12"));

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hinzugefuegtAm").value("2026-11-12"));
    }

    @Test
    @DisplayName("US-03.1 AK2: gleichzeitiges doppeltes Merken ergibt genau einen Eintrag (D-21)")
    void gleichzeitigesMerken() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> merken =
                () -> {
                    start.await();
                    return merke(ich, kopfhoerer).andReturn().getResponse().getStatus();
                };
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> erster = pool.submit(merken);
            Future<Integer> zweiter = pool.submit(merken);
            start.countDown();
            assertThat(List.of(erster.get(), zweiter.get())).containsExactlyInAnyOrder(201, 200);
        }
        mockMvc.perform(get(MERKLISTE, ich)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName(
            "US-03.1 AK3: neueste zuerst, mit Merkdatum, aktuellem Listenpreis und Verfügbarkeit")
    void beobachteteArtikel() throws Exception {
        merke(ich, kopfhoerer);
        uhr.stelle(AM_15_11);
        merke(ich, kabel);
        jdbc.sql("UPDATE produkt SET listenpreis_cent = 6999 WHERE id = :id")
                .param("id", kopfhoerer)
                .update();

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].produktId").value(kabel.toString()))
                .andExpect(jsonPath("$[0].titel").value("Kabel"))
                .andExpect(jsonPath("$[0].hinzugefuegtAm").value("2026-11-15"))
                .andExpect(jsonPath("$[0].listenpreis.cent").value(999))
                .andExpect(jsonPath("$[0].listenpreis.waehrung").value("EUR"))
                .andExpect(jsonPath("$[0].istVerfuegbar").value(false))
                .andExpect(jsonPath("$[1].titel").value("Kopfhörer"))
                .andExpect(jsonPath("$[1].hinzugefuegtAm").value("2026-11-12"))
                .andExpect(jsonPath("$[1].listenpreis.cent").value(6999))
                .andExpect(jsonPath("$[1].istVerfuegbar").value(true));
    }

    @Test
    @DisplayName("US-03.1 AK3: am selben Tag zuletzt Gemerktes steht vorn (D-21)")
    void selberTag() throws Exception {
        merke(ich, kopfhoerer);
        uhr.stelle(AM_12_11.plusSeconds(60));
        merke(ich, kabel);

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(jsonPath("$[0].titel").value("Kabel"))
                .andExpect(jsonPath("$[1].titel").value("Kopfhörer"));
    }

    @Test
    @DisplayName("US-03.1 AK1: Merkdatum gilt in der Zeitzone des Shops (D-21)")
    void merkdatumInZeitzone() throws Exception {
        uhr.stelle(Instant.parse("2026-11-14T23:30:00Z"));

        merke(ich, kabel).andExpect(jsonPath("$.hinzugefuegtAm").value("2026-11-15"));
    }

    @Test
    @DisplayName("US-03.1 AK3: Abruf der Merkliste ohne N+1 (2 Abfragen bei 1 und 2 Einträgen)")
    void abrufOhneNPlusEins() throws Exception {
        merke(ich, kopfhoerer);
        long beiEinem = abfragenFuerAbruf();
        merke(ich, kabel);

        assertThat(beiEinem).isEqualTo(2);
        assertThat(abfragenFuerAbruf()).isEqualTo(2);
    }

    @Test
    @DisplayName("US-03.1 AK4: Listen sind persönlich")
    void listenSindPersoenlich() throws Exception {
        merke(andererKunde, kabel);
        merke(ich, kopfhoerer);
        entferne(ich, kopfhoerer);

        mockMvc.perform(get(MERKLISTE, ich)).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get(EINTRAG, ich, kabel)).andExpect(status().isNotFound());
        mockMvc.perform(get(MERKLISTE, andererKunde))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titel").value("Kabel"))
                .andExpect(jsonPath("$[0].hinzugefuegtAm").value("2026-11-12"));
    }

    @Test
    @DisplayName("US-03.1 AK5: leere Merkliste ist eine leere Liste, kein Fehler")
    void leereMerkliste() throws Exception {
        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("US-03.2 AK1: entferntes Produkt verschwindet, der Rest bleibt (204)")
    void produktEntfernen() throws Exception {
        merke(ich, kopfhoerer);
        merke(ich, kabel);

        entferne(ich, kopfhoerer).andExpect(status().isNoContent());

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titel").value("Kabel"));
        mockMvc.perform(get(EINTRAG, ich, kopfhoerer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Nicht gefunden"));
    }

    @Test
    @DisplayName("US-03.2 AK2: nicht gemerktes Produkt entfernen ändert nichts, kein Fehler")
    void nichtGemerktesEntfernen() throws Exception {
        merke(ich, kabel);

        entferne(ich, kopfhoerer).andExpect(status().isNoContent());
        entferne(ich, UUID.randomUUID()).andExpect(status().isNoContent());

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titel").value("Kabel"));
    }

    @Test
    @DisplayName("US-03.2 AK3: erneut merken nach Entfernen setzt das neue Datum")
    void erneutMerken() throws Exception {
        merke(ich, kopfhoerer);
        entferne(ich, kopfhoerer);

        uhr.stelle(AM_20_11);
        merke(ich, kopfhoerer)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hinzugefuegtAm").value("2026-11-20"));

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hinzugefuegtAm").value("2026-11-20"));
    }

    @Test
    @DisplayName("US-03.2 AK4: Entfernen bei mir lässt die Liste eines anderen Kunden unverändert")
    void entfernenIstPersoenlich() throws Exception {
        merke(andererKunde, kopfhoerer);
        merke(ich, kopfhoerer);

        entferne(ich, kopfhoerer).andExpect(status().isNoContent());

        mockMvc.perform(get(MERKLISTE, andererKunde))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titel").value("Kopfhörer"));
    }

    @Test
    @DisplayName("US-03.2 AK5: unbekannter Kunde ergibt 404 bei Abruf, Merken und Entfernen")
    void unbekannterKunde() throws Exception {
        UUID unbekannt = UUID.randomUUID();

        mockMvc.perform(get(MERKLISTE, unbekannt))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Nicht gefunden"));
        merke(unbekannt, kopfhoerer).andExpect(status().isNotFound());
        entferne(unbekannt, kopfhoerer).andExpect(status().isNotFound());
        mockMvc.perform(get(EINTRAG, unbekannt, kopfhoerer)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("US-03.2 AK5: unbekanntes Produkt merken ergibt 404, Liste bleibt leer")
    void unbekanntesProdukt() throws Exception {
        merke(ich, UUID.randomUUID())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Nicht gefunden"));

        mockMvc.perform(get(MERKLISTE, ich)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("D-07: wird ein Produkt aus dem Katalog entfernt, verschwindet sein Eintrag")
    void produktAusKatalogEntfernt() throws Exception {
        merke(ich, kopfhoerer);
        merke(ich, kabel);

        produkte.deleteById(kopfhoerer);

        mockMvc.perform(get(MERKLISTE, ich))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titel").value("Kabel"));
    }

    @Test
    @DisplayName("400: ungültige Produkt-ID im Pfad")
    void ungueltigeProduktId() throws Exception {
        mockMvc.perform(put(EINTRAG, ich, "keine-uuid")).andExpect(status().isBadRequest());
    }

    private ResultActions merke(UUID kundenId, UUID produktId) throws Exception {
        return mockMvc.perform(put(EINTRAG, kundenId, produktId));
    }

    private ResultActions entferne(UUID kundenId, UUID produktId) throws Exception {
        return mockMvc.perform(delete(EINTRAG, kundenId, produktId));
    }

    private long abfragenFuerAbruf() throws Exception {
        Statistics statistik = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistik.setStatisticsEnabled(true);
        statistik.clear();
        try {
            mockMvc.perform(get(MERKLISTE, ich)).andExpect(status().isOk());
            return statistik.getPrepareStatementCount();
        } finally {
            statistik.setStatisticsEnabled(false);
        }
    }
}
