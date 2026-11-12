package de.bestellsystem.versand.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.VerstellbareUhrKonfiguration;
import de.bestellsystem.VerstellbareUhrKonfiguration.VerstellbareUhr;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** US-07.1 bis US-07.3 und D-22 über HTTP. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, VerstellbareUhrKonfiguration.class})
class VersandApiIT {

    private static final String BESTELLUNGEN = "/api/v1/kunden/{kundenId}/bestellungen";
    private static final String BESTELLUNG = BESTELLUNGEN + "/{bestellnummer}";
    private static final String PAKETE = BESTELLUNG + "/pakete";
    private static final String PAKET = PAKETE + "/{paketId}";
    private static final String STATUS = PAKET + "/status";
    private static final Instant AM_12_11 = Instant.parse("2026-11-12T09:30:00Z");
    private static final Instant AM_13_11 = Instant.parse("2026-11-13T08:00:00Z");
    private static final String DHL_NUMMER = "00340434161094042557";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcClient jdbc;
    @Autowired private ProduktRepository produkte;
    @Autowired private KundeService kundeService;
    @Autowired private VerstellbareUhr uhr;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private Produkt artikel;
    private UUID kundenId;
    private String bestellnummer;

    @BeforeEach
    void bestellung() throws Exception {
        Datenbestand.leere(jdbc);
        uhr.stelle(AM_12_11);
        artikel = produkte.save(new Produkt("Artikel", Geld.ausCent(10_000), 10));
        kundenId = registriere("anna@example.de");
        bestellnummer = gibAuf(kundenId);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Nested
    class PaketAnlegen {

        @Test
        @DisplayName("US-07.1 AK1: Paket anlegen, Status ANGELEGT, Zeitpunkte 12.11.2026")
        void anlegen() throws Exception {
            String paketId =
                    id(
                            legeAn("DHL", DHL_NUMMER)
                                    .andExpect(status().isCreated())
                                    .andExpect(
                                            header().string(
                                                            "Location",
                                                            matchesPattern(
                                                                    ".*/api/v1/kunden/%s/bestellungen/%s/pakete/[0-9a-f-]{36}"
                                                                            .formatted(
                                                                                    kundenId,
                                                                                    bestellnummer))))
                                    .andExpect(jsonPath("$.dienstleister").value("DHL"))
                                    .andExpect(jsonPath("$.sendungsnummer").value(DHL_NUMMER))
                                    .andExpect(jsonPath("$.lieferstatus").value("ANGELEGT"))
                                    .andExpect(
                                            jsonPath("$.angelegtAm").value("2026-11-12T09:30:00Z"))
                                    .andExpect(
                                            jsonPath("$.statusGeaendertAm")
                                                    .value("2026-11-12T09:30:00Z")));

            mockMvc.perform(get(PAKET, kundenId, bestellnummer, paketId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lieferstatus").value("ANGELEGT"));
            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.pakete.length()").value(1))
                    .andExpect(jsonPath("$.pakete[0].id").value(paketId));
        }

        @Test
        @DisplayName("US-07.1 AK2: Bestellung ohne Paket ist gültig")
        void ohnePaket() throws Exception {
            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pakete", empty()));
        }

        @Test
        @DisplayName("US-07.1 AK3: mehrere Pakete je Bestellung in Anlagereihenfolge")
        void mehrerePakete() throws Exception {
            legeAn("DHL", DHL_NUMMER).andExpect(status().isCreated());
            legeAn("DPD", "01234567890123").andExpect(status().isCreated());

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.pakete[*].dienstleister", contains("DHL", "DPD")))
                    .andExpect(
                            jsonPath(
                                    "$.pakete[*].sendungsnummer",
                                    contains(DHL_NUMMER, "01234567890123")));
        }

        @Test
        @DisplayName("US-07.1 AK4: Sendungsnummer wird getrimmt und großgeschrieben")
        void normalisiert() throws Exception {
            legeAn("DHL", " ab123de ")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sendungsnummer").value("AB123DE"));
        }

        @Test
        @DisplayName(
                "US-07.1 AK5: Sendungsnummer eindeutig je Dienstleister über alle Bestellungen")
        void eindeutigJeDienstleister() throws Exception {
            legeAn("DHL", "AB123DE").andExpect(status().isCreated());
            String andereBestellung = gibAuf(registriere("bernd@example.de"));
            UUID bernd = kundeVon(andereBestellung);

            mockMvc.perform(
                            post(PAKETE, bernd, andereBestellung)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(paketJson("DHL", "ab123de")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Konflikt"));
            legeAn("DHL", "ab123de").andExpect(status().isConflict());

            legeAn("GLS", "AB123DE").andExpect(status().isCreated());
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "{\"sendungsnummer\": \"AB123\"}",
                    "{\"dienstleister\": \"POST\", \"sendungsnummer\": \"AB123\"}",
                    "{\"dienstleister\": \"DHL\"}",
                    "{\"dienstleister\": \"DHL\", \"sendungsnummer\": \"  \"}",
                    "{\"dienstleister\": \"DHL\", \"sendungsnummer\": \"AB-123\"}",
                    "{\"dienstleister\": \"DHL\", \"sendungsnummer\":"
                            + " \"123456789012345678901234567890123456789012345678901\"}"
                })
        @DisplayName("US-07.1 AK6: ungültige Eingabe ergibt 400")
        void ungueltig(String json) throws Exception {
            mockMvc.perform(
                            post(PAKETE, kundenId, bestellnummer)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.pakete", empty()));
        }

        @Test
        @DisplayName("US-07.1 AK7: unbekannte oder fremde Bestellung ergibt 404")
        void unbekannteBestellung() throws Exception {
            mockMvc.perform(
                            post(PAKETE, kundenId, "B-2026-999999")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(paketJson("DHL", DHL_NUMMER)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Nicht gefunden"));
            mockMvc.perform(
                            post(PAKETE, registriere("bernd@example.de"), bestellnummer)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(paketJson("DHL", DHL_NUMMER)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class LieferstatusAktualisieren {

        private String paketId;

        @BeforeEach
        void paket() throws Exception {
            paketId = id(legeAn("DHL", DHL_NUMMER).andExpect(status().isCreated()));
        }

        @Test
        @DisplayName("US-07.2 AK1: nächste Stufe am 13.11.2026, angelegt bleibt 12.11.2026")
        void naechsteStufe() throws Exception {
            uhr.stelle(AM_13_11);

            setze("UEBERGEBEN")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lieferstatus").value("UEBERGEBEN"))
                    .andExpect(jsonPath("$.statusGeaendertAm").value("2026-11-13T08:00:00Z"))
                    .andExpect(jsonPath("$.angelegtAm").value("2026-11-12T09:30:00Z"));

            mockMvc.perform(get(PAKET, kundenId, bestellnummer, paketId))
                    .andExpect(jsonPath("$.lieferstatus").value("UEBERGEBEN"))
                    .andExpect(jsonPath("$.statusGeaendertAm").value("2026-11-13T08:00:00Z"));
        }

        @Test
        @DisplayName("US-07.2 AK2: Stufen überspringen")
        void ueberspringen() throws Exception {
            setze("ZUGESTELLT")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lieferstatus").value("ZUGESTELLT"));
        }

        @Test
        @DisplayName("US-07.2 AK3: RETOURE und VERLOREN erst nach Übergabe")
        void retoureNachUebergabe() throws Exception {
            setze("RETOURE")
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Fachlich abgelehnt"));
            setze("VERLOREN").andExpect(status().isUnprocessableEntity());

            setze("UEBERGEBEN").andExpect(status().isOk());
            setze("RETOURE")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lieferstatus").value("RETOURE"));
        }

        @Test
        @DisplayName("US-07.2 AK4: nicht rückwärts, nicht derselbe Status, Status bleibt")
        void nichtRueckwaerts() throws Exception {
            setze("IN_ZUSTELLUNG").andExpect(status().isOk());

            setze("UEBERGEBEN").andExpect(status().isUnprocessableEntity());
            setze("ANGELEGT").andExpect(status().isUnprocessableEntity());
            setze("IN_ZUSTELLUNG").andExpect(status().isUnprocessableEntity());

            mockMvc.perform(get(PAKET, kundenId, bestellnummer, paketId))
                    .andExpect(jsonPath("$.lieferstatus").value("IN_ZUSTELLUNG"));
        }

        @Test
        @DisplayName("US-07.2 AK5: Endzustand lehnt jeden Wechsel ab")
        void endzustand() throws Exception {
            setze("ZUGESTELLT").andExpect(status().isOk());

            setze("RETOURE").andExpect(status().isUnprocessableEntity());
            setze("IN_ZUSTELLUNG").andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("US-07.2 AK6: erlaubte Folgestatus in der Antwort")
        void erlaubteFolgestatus() throws Exception {
            setze("UEBERGEBEN")
                    .andExpect(
                            jsonPath(
                                    "$.erlaubteFolgestatus",
                                    contains(
                                            "IN_ZUSTELLUNG", "ZUGESTELLT", "RETOURE", "VERLOREN")));
            setze("VERLOREN").andExpect(jsonPath("$.erlaubteFolgestatus", empty()));
        }

        @Test
        @DisplayName("US-07.2 AK7: Status fehlt oder unbekannt 400, fremdes Paket 404")
        void ungueltigUndUnbekannt() throws Exception {
            mockMvc.perform(
                            put(STATUS, kundenId, bestellnummer, paketId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest());
            setze("UNTERWEGS").andExpect(status().isBadRequest());

            String andereBestellung = gibAuf(kundenId);
            mockMvc.perform(
                            put(STATUS, kundenId, andereBestellung, paketId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"lieferstatus\": \"UEBERGEBEN\"}"))
                    .andExpect(status().isNotFound());
            mockMvc.perform(
                            put(STATUS, kundenId, "B-2026-999999", paketId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"lieferstatus\": \"UEBERGEBEN\"}"))
                    .andExpect(status().isNotFound());
        }

        private ResultActions setze(String lieferstatus) throws Exception {
            return mockMvc.perform(
                    put(STATUS, kundenId, bestellnummer, paketId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"lieferstatus\": \"%s\"}".formatted(lieferstatus)));
        }
    }

    @Nested
    class PaketeSehen {

        @Test
        @DisplayName("US-07.3 AK1: beide Pakete mit Dienstleister, Nummer, Status und Zeitpunkten")
        void paketeInDerBestellansicht() throws Exception {
            String dhl = id(legeAn("DHL", "AB123DE"));
            legeAn("DPD", "XY9").andExpect(status().isCreated());
            uhr.stelle(AM_13_11);
            mockMvc.perform(
                            put(STATUS, kundenId, bestellnummer, dhl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"lieferstatus\": \"IN_ZUSTELLUNG\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.pakete.length()").value(2))
                    .andExpect(jsonPath("$.pakete[0].dienstleister").value("DHL"))
                    .andExpect(jsonPath("$.pakete[0].sendungsnummer").value("AB123DE"))
                    .andExpect(jsonPath("$.pakete[0].lieferstatus").value("IN_ZUSTELLUNG"))
                    .andExpect(jsonPath("$.pakete[0].angelegtAm").value("2026-11-12T09:30:00Z"))
                    .andExpect(
                            jsonPath("$.pakete[0].statusGeaendertAm").value("2026-11-13T08:00:00Z"))
                    .andExpect(jsonPath("$.pakete[1].dienstleister").value("DPD"))
                    .andExpect(jsonPath("$.pakete[1].sendungsnummer").value("XY9"))
                    .andExpect(jsonPath("$.pakete[1].lieferstatus").value("ANGELEGT"));
        }

        @Test
        @DisplayName("US-07.3 AK3: fremde Bestellung und fremdes Paket bleiben verborgen")
        void fremdePakete() throws Exception {
            String paketId = id(legeAn("DHL", DHL_NUMMER));
            UUID bernd = registriere("bernd@example.de");

            mockMvc.perform(get(BESTELLUNG, bernd, bestellnummer)).andExpect(status().isNotFound());
            mockMvc.perform(get(PAKET, bernd, bestellnummer, paketId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Nicht gefunden"));
            mockMvc.perform(get(PAKET, kundenId, bestellnummer, UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("US-07.3 AK4: Bestellansicht braucht unabhängig von der Paketzahl 3 Abfragen")
        void ohneNPlusEins() throws Exception {
            legeAn("DHL", "A1").andExpect(status().isCreated());
            assertThat(abfragenFuerAbruf()).isEqualTo(3);

            legeAn("DPD", "A2").andExpect(status().isCreated());
            legeAn("GLS", "A3").andExpect(status().isCreated());
            assertThat(abfragenFuerAbruf()).isEqualTo(3);
        }

        private long abfragenFuerAbruf() throws Exception {
            Statistics statistik =
                    entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
            statistik.setStatisticsEnabled(true);
            statistik.clear();
            try {
                mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                        .andExpect(status().isOk());
                return statistik.getPrepareStatementCount();
            } finally {
                statistik.setStatisticsEnabled(false);
            }
        }
    }

    private UUID registriere(String email) {
        Kunde kunde =
                kundeService.registriere(
                        "Anna",
                        "Muster",
                        email,
                        new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                        false);
        return kunde.getId();
    }

    private String gibAuf(UUID kunde) throws Exception {
        UUID lieferadresse = kundeService.finde(kunde).getAdressen().getFirst().getId();
        return mockMvc.perform(
                        post(BESTELLUNGEN, kunde)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"positionen": [{"produktId": "%s", "menge": 1}],
                                         "lieferadresseId": "%s", "zahlungsart": "RECHNUNG"}
                                        """
                                                .formatted(artikel.getId(), lieferadresse)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location")
                .replaceAll(".*/", "");
    }

    private UUID kundeVon(String nummer) {
        return jdbc.sql("SELECT kunde_id FROM bestellung WHERE bestellnummer = ?")
                .param(nummer)
                .query(UUID.class)
                .single();
    }

    private ResultActions legeAn(String dienstleister, String sendungsnummer) throws Exception {
        return mockMvc.perform(
                post(PAKETE, kundenId, bestellnummer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paketJson(dienstleister, sendungsnummer)));
    }

    private static String paketJson(String dienstleister, String sendungsnummer) {
        return "{\"dienstleister\": \"%s\", \"sendungsnummer\": \"%s\"}"
                .formatted(dienstleister, sendungsnummer);
    }

    private static String id(ResultActions antwort) throws Exception {
        return JsonPath.read(antwort.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
