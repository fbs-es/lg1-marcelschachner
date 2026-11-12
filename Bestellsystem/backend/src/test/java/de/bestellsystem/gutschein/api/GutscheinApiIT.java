package de.bestellsystem.gutschein.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.persistence.GutscheinRepository;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** US-08.2 über HTTP; die feste Uhr steht auf dem 12.11.2026 (D-23). */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class GutscheinApiIT {

    private static final String GUTSCHEINE = "/api/v1/gutscheine";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcClient jdbc;
    @Autowired private GutscheinRepository gutscheine;
    @Autowired private ProduktRepository produkte;
    @Autowired private KundeService kundeService;

    @BeforeEach
    @AfterEach
    void leere() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("US-08.2 AK1: herbst10 mit 10 % wird als HERBST10 angelegt, 201 mit Location")
    void prozentgutschein() throws Exception {
        String ort =
                legeAn(prozentJson(" herbst10 ", "10", "2026-12-31"))
                        .andExpect(status().isCreated())
                        .andExpect(
                                header().string(
                                                "Location",
                                                matchesPattern(
                                                        ".*" + GUTSCHEINE + "/[0-9a-f-]{36}")))
                        .andExpect(jsonPath("$.code").value("HERBST10"))
                        .andExpect(jsonPath("$.rabattArt").value("PROZENT"))
                        .andExpect(jsonPath("$.prozent").value(10))
                        .andExpect(jsonPath("$.festbetrag").doesNotExist())
                        .andExpect(jsonPath("$.ablaufdatum").value("2026-12-31"))
                        .andExpect(jsonPath("$.istGueltig").value(true))
                        .andReturn()
                        .getResponse()
                        .getHeader("Location");

        mockMvc.perform(get(ort))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("HERBST10"));
    }

    @Test
    @DisplayName("US-08.2 AK2: MINUS5 mit Festbetrag 5,00 € wird angelegt")
    void festbetrag() throws Exception {
        legeAn(festbetragJson("MINUS5", 500, "2026-12-31"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rabattArt").value("FESTBETRAG"))
                .andExpect(jsonPath("$.festbetrag.cent").value(500))
                .andExpect(jsonPath("$.festbetrag.waehrung").value("EUR"))
                .andExpect(jsonPath("$.prozent").doesNotExist());
    }

    @Test
    @DisplayName("US-08.2 AK3: ungültiger Rabattwert oder unpassend zur Rabattart: 400")
    void ungueltigerRabattwert() throws Exception {
        for (String prozent : new String[] {"0", "100.01", "10.555"}) {
            legeAn(prozentJson("X", prozent, "2026-12-31"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Ungültige Eingabe"));
        }
        legeAn(festbetragJson("X", 0, "2026-12-31")).andExpect(status().isBadRequest());
        legeAn(
                        """
                        {"code": "X", "rabattArt": "PROZENT", "prozent": 10,
                         "festbetrag": {"cent": 500, "waehrung": "EUR"},
                         "ablaufdatum": "2026-12-31"}
                        """)
                .andExpect(status().isBadRequest());
        legeAn(
                        """
                        {"code": "X", "rabattArt": "FESTBETRAG", "ablaufdatum": "2026-12-31"}
                        """)
                .andExpect(status().isBadRequest());
        legeAn(
                        """
                        {"code": "X", "prozent": 10, "ablaufdatum": "2026-12-31"}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("rabattArt")));
    }

    @Test
    @DisplayName("US-08.2 AK4: Code mit unerlaubten Zeichen, leer oder zu lang: 400")
    void ungueltigerCode() throws Exception {
        for (String code : new String[] {"HERBST 10", "RABATT!", " ", "A".repeat(51)}) {
            legeAn(prozentJson(code, "10", "2026-12-31"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Ungültige Eingabe"));
        }
    }

    @Test
    @DisplayName("US-08.2 AK4: vergebener Code, auch in anderer Schreibweise: 409")
    void codeVergeben() throws Exception {
        legeAn(prozentJson("HERBST10", "10", "2026-12-31")).andExpect(status().isCreated());

        legeAn(prozentJson("Herbst10", "20", "2026-12-31"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Konflikt"));
    }

    @Test
    @DisplayName("US-08.2 AK5: Ablaufdatum 11.11.2026 abgelehnt (422), 12.11.2026 erlaubt")
    void ablaufdatum() throws Exception {
        legeAn(prozentJson("GESTERN", "10", "2026-11-11"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Fachlich abgelehnt"));
        legeAn(prozentJson("HEUTE", "10", "2026-11-12")).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("US-08.2 AK6: Liste nach Code sortiert mit Rabatt, Ablaufdatum und Gültigkeit")
    void liste() throws Exception {
        gutscheine.save(
                Gutschein.prozent("SOMMER20", new BigDecimal("20"), LocalDate.of(2026, 8, 31)));
        legeAn(festbetragJson("MINUS5", 500, "2026-12-31")).andExpect(status().isCreated());
        legeAn(prozentJson("HERBST10", "10", "2026-11-30")).andExpect(status().isCreated());

        mockMvc.perform(get(GUTSCHEINE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("HERBST10"))
                .andExpect(jsonPath("$[0].istGueltig").value(true))
                .andExpect(jsonPath("$[1].code").value("MINUS5"))
                .andExpect(jsonPath("$[1].festbetrag.cent").value(500))
                .andExpect(jsonPath("$[2].code").value("SOMMER20"))
                .andExpect(jsonPath("$[2].prozent").value(20))
                .andExpect(jsonPath("$[2].istGueltig").value(false));
    }

    @Test
    @DisplayName("US-08.2 AK6: leere Liste und unbekannter Gutschein 404")
    void leereListe() throws Exception {
        mockMvc.perform(get(GUTSCHEINE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get(GUTSCHEINE + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("US-08.2 AK7: angelegter Gutschein ist im Checkout einlösbar")
    void sofortEinloesbar() throws Exception {
        legeAn(prozentJson("herbst10", "10", "2026-12-31")).andExpect(status().isCreated());
        Produkt buch = produkte.save(new Produkt("Buch", Geld.ausCent(5500), 5));
        Kunde kunde =
                kundeService.registriere(
                        "Anna",
                        "Muster",
                        "anna@example.de",
                        new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                        false);

        mockMvc.perform(
                        post("/api/v1/kunden/{kundenId}/bestellungen", kunde.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"positionen": [{"produktId": "%s", "menge": 1}],
                                         "lieferadresseId": "%s", "zahlungsart": "RECHNUNG",
                                         "gutscheincode": "Herbst10"}
                                        """
                                                .formatted(
                                                        buch.getId(),
                                                        kunde.getAdressen().getFirst().getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gutscheincode").value("HERBST10"))
                .andExpect(jsonPath("$.rabattBetrag.cent").value(550));
    }

    private ResultActions legeAn(String json) throws Exception {
        return mockMvc.perform(
                post(GUTSCHEINE).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String prozentJson(String code, String prozent, String ablaufdatum) {
        return """
        {"code": "%s", "rabattArt": "PROZENT", "prozent": %s, "ablaufdatum": "%s"}
        """
                .formatted(code, prozent, ablaufdatum);
    }

    private static String festbetragJson(String code, long cent, String ablaufdatum) {
        return """
        {"code": "%s", "rabattArt": "FESTBETRAG",
         "festbetrag": {"cent": %d, "waehrung": "EUR"}, "ablaufdatum": "%s"}
        """
                .formatted(code, cent, ablaufdatum);
    }
}
