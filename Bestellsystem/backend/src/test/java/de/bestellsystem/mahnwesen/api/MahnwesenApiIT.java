package de.bestellsystem.mahnwesen.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.BenachrichtigungsAttrappe;
import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.mahnwesen.Mahnbestand;
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

/** US-06.2 AK5 über HTTP; Stichtag ist der 12.11.2026 der festen Uhr. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({
    TestcontainersKonfiguration.class,
    FesteUhrKonfiguration.class,
    BenachrichtigungsAttrappe.Konfiguration.class
})
class MahnwesenApiIT {

    private static final String MAHNPRUEFUNG = "/api/v1/kunden/{kundenId}/mahnpruefung";
    private static final String MAHNLAEUFE = "/api/v1/mahnlaeufe";

    @Autowired private MockMvc mockMvc;
    @Autowired private BenachrichtigungsAttrappe versand;
    @Autowired private JdbcClient jdbc;
    @Autowired private KundeService kundeService;
    @Autowired private ProduktRepository produkte;
    @Autowired private BestellungRepository bestellungen;

    private Mahnbestand bestand;
    private UUID anna;

    @BeforeEach
    void ueberfaelligeBestellung() {
        Datenbestand.leere(jdbc);
        versand.zuruecksetzen();
        bestand = new Mahnbestand(kundeService, produkte, bestellungen, jdbc);
        anna = bestand.kunde("Anna", "anna@example.de");
        bestand.bestellung(anna, "B-1", LocalDate.of(2026, 10, 1), 5_000, 0);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("US-06.2 AK5: Mahnprüfung liefert gemahnte Bestellungen mit offenem Betrag")
    void mahnpruefung() throws Exception {
        mockMvc.perform(post(MAHNPRUEFUNG, anna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stichtag").value("2026-11-12"))
                .andExpect(jsonPath("$.istGemahnt").value(true))
                .andExpect(jsonPath("$.mahnpositionen.length()").value(1))
                .andExpect(jsonPath("$.mahnpositionen[0].bestellnummer").value("B-1"))
                .andExpect(jsonPath("$.mahnpositionen[0].faelligAm").value("2026-10-15"))
                .andExpect(jsonPath("$.mahnpositionen[0].offenerBetrag.cent").value(5_000))
                .andExpect(jsonPath("$.mahnpositionen[0].offenerBetrag.waehrung").value("EUR"));

        mockMvc.perform(post(MAHNPRUEFUNG, anna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.istGemahnt").value(false))
                .andExpect(jsonPath("$.mahnpositionen.length()").value(0));
        assertThat(versand.versendet()).hasSize(1);
    }

    @Test
    @DisplayName("US-06.2 AK5: unbekannter Kunde 404 als ProblemDetail")
    void unbekannterKunde() throws Exception {
        mockMvc.perform(post(MAHNPRUEFUNG, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Nicht gefunden"));
    }

    @Test
    @DisplayName("US-06.2 AK5: Versandfehler 503, keine Mahnung protokolliert")
    void versandfehler() throws Exception {
        versand.scheitereFuer("anna@example.de");

        mockMvc.perform(post(MAHNPRUEFUNG, anna))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Benachrichtigung nicht versendet"));
        assertThat(bestand.mahnungen()).isEmpty();
    }

    @Test
    @DisplayName("US-06.2 AK5: Mahnlauf liefert Stichtag und Zähler")
    void mahnlauf() throws Exception {
        UUID ben = bestand.kunde("Ben", "ben@example.de");
        bestand.bestellung(ben, "B-6", LocalDate.of(2026, 10, 6), 3_000, 0);
        versand.scheitereFuer("ben@example.de");

        mockMvc.perform(post(MAHNLAEUFE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stichtag").value("2026-11-12"))
                .andExpect(jsonPath("$.gemahnteKunden").value(1))
                .andExpect(jsonPath("$.fehlgeschlageneKunden").value(1));
    }
}
