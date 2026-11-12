package de.bestellsystem.zahlung.api;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** US-05.1 und D-19 über HTTP; jede Bestellung hat einen Gesamtwert von 100,00 €. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class ZahlungApiIT {

    private static final String BESTELLUNGEN = "/api/v1/kunden/{kundenId}/bestellungen";
    private static final String BESTELLUNG = BESTELLUNGEN + "/{bestellnummer}";
    private static final String ZAHLUNG = BESTELLUNG + "/zahlungen/{zahlungId}";
    private static final String STATUS = ZAHLUNG + "/status";
    private static final String ERSTATTUNGEN = BESTELLUNG + "/erstattungen";
    private static final Anschrift HAUPTSTRASSE =
            new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcClient jdbc;
    @Autowired private ProduktRepository produkte;
    @Autowired private GutscheinRepository gutscheine;
    @Autowired private KundeService kundeService;

    private Produkt artikel;
    private UUID kundenId;
    private UUID lieferadresse;
    private String bestellnummer;
    private String ersteZahlung;

    @BeforeEach
    void bestellungUeber100Euro() throws Exception {
        Datenbestand.leere(jdbc);
        artikel = produkte.save(new Produkt("Artikel", Geld.ausCent(10_000), 10));
        Kunde kunde =
                kundeService.registriere("Anna", "Muster", "anna@example.de", HAUPTSTRASSE, false);
        kundenId = kunde.getId();
        lieferadresse = kunde.getAdressen().getFirst().getId();
        gutscheine.save(
                Gutschein.festbetrag("MINUS10", Geld.ausCent(1_000), LocalDate.of(2026, 12, 31)));
        bestellnummer = gibAuf(null);
        ersteZahlung = ersteZahlung(bestellnummer);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Nested
    class Bezahlstatus {

        @Test
        @DisplayName("US-05.1 AK1: nur ausstehende Zahlung, nicht bezahlt, offen 100,00 €")
        void nurAusstehend() throws Exception {
            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.istVollstaendigBezahlt").value(false))
                    .andExpect(jsonPath("$.bezahlteSumme.cent").value(0))
                    .andExpect(jsonPath("$.offenerBetrag.cent").value(10_000))
                    .andExpect(jsonPath("$.offenerBetrag.waehrung").value("EUR"));
        }

        @Test
        @DisplayName("US-05.1 AK2: Zahlung erfolgreich, vollständig bezahlt, offen 0,00 €")
        void vollstaendig() throws Exception {
            setze(ersteZahlung, "ERFOLGREICH")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ersteZahlung))
                    .andExpect(jsonPath("$.transaktionsstatus").value("ERFOLGREICH"))
                    .andExpect(jsonPath("$.zahlungszeitpunkt").value("2026-11-12T09:30:00Z"));

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.istVollstaendigBezahlt").value(true))
                    .andExpect(jsonPath("$.bezahlteSumme.cent").value(10_000))
                    .andExpect(jsonPath("$.offenerBetrag.cent").value(0))
                    .andExpect(jsonPath("$.zahlungen[0].transaktionsstatus").value("ERFOLGREICH"));
        }

        @Test
        @DisplayName("US-05.1 AK4: fehlgeschlagene Zahlung zählt nicht, offen 100,00 €")
        void fehlgeschlagen() throws Exception {
            setze(ersteZahlung, "FEHLGESCHLAGEN")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.zahlungszeitpunkt").value(nullValue()));

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.istVollstaendigBezahlt").value(false))
                    .andExpect(jsonPath("$.offenerBetrag.cent").value(10_000));
        }

        @Test
        @DisplayName("US-05.1 AK5: Erstattung 30,00 € ergibt bezahlte Summe 70,00 €")
        void erstattung() throws Exception {
            setze(ersteZahlung, "ERFOLGREICH").andExpect(status().isOk());
            String erstattung = id(erstatte(3_000, "RECHNUNG").andExpect(status().isCreated()));
            setze(erstattung, "ERFOLGREICH").andExpect(status().isOk());

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.bezahlteSumme.cent").value(7_000))
                    .andExpect(jsonPath("$.istVollstaendigBezahlt").value(false))
                    .andExpect(jsonPath("$.offenerBetrag.cent").value(3_000))
                    .andExpect(jsonPath("$.zahlungen.length()").value(2));
        }

        @Test
        @DisplayName("US-05.1 AK7: Rabatt 10,00 €, erfolgreiche Zahlung 90,00 € genügt")
        void mitRabatt() throws Exception {
            String mitGutschein = gibAuf("MINUS10");
            setze(mitGutschein, ersteZahlung(mitGutschein), "ERFOLGREICH")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.betrag.cent").value(9_000));

            mockMvc.perform(get(BESTELLUNG, kundenId, mitGutschein))
                    .andExpect(jsonPath("$.warenwert.cent").value(10_000))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(9_000))
                    .andExpect(jsonPath("$.istVollstaendigBezahlt").value(true))
                    .andExpect(jsonPath("$.offenerBetrag.cent").value(0));
        }

        @Test
        @DisplayName("D-19: Historie zeigt je Bestellung offenen Betrag und Bezahlstatus")
        void historie() throws Exception {
            String bezahlt = gibAuf(null);
            setze(bezahlt, ersteZahlung(bezahlt), "ERFOLGREICH").andExpect(status().isOk());

            mockMvc.perform(get(BESTELLUNGEN, kundenId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].bestellnummer").value(bezahlt))
                    .andExpect(jsonPath("$[0].istVollstaendigBezahlt").value(true))
                    .andExpect(jsonPath("$[0].offenerBetrag.cent").value(0))
                    .andExpect(jsonPath("$[1].bestellnummer").value(bestellnummer))
                    .andExpect(jsonPath("$[1].istVollstaendigBezahlt").value(false))
                    .andExpect(jsonPath("$[1].offenerBetrag.cent").value(10_000));
        }
    }

    @Nested
    class StatusAendern {

        @Test
        @DisplayName("D-19: Endzustand ist endgültig, zweiter Wechsel ergibt 422")
        void endgueltig() throws Exception {
            setze(ersteZahlung, "STORNIERT").andExpect(status().isOk());

            setze(ersteZahlung, "ERFOLGREICH")
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Fachlich abgelehnt"));
            mockMvc.perform(get(ZAHLUNG, kundenId, bestellnummer, ersteZahlung))
                    .andExpect(jsonPath("$.transaktionsstatus").value("STORNIERT"));
        }

        @Test
        @DisplayName("D-19: AUSSTEHEND als Ziel ergibt 422")
        void zielAusstehend() throws Exception {
            setze(ersteZahlung, "AUSSTEHEND").andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("D-19: fehlender oder unbekannter Status ergibt 400")
        void ungueltigerStatus() throws Exception {
            mockMvc.perform(
                            put(STATUS, kundenId, bestellnummer, ersteZahlung)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fehler[0].feld").value("transaktionsstatus"));
            setze(ersteZahlung, "BEZAHLT").andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("D-19: unbekannte Zahlung ergibt 404")
        void unbekannteZahlung() throws Exception {
            setze(UUID.randomUUID().toString(), "ERFOLGREICH").andExpect(status().isNotFound());
            mockMvc.perform(get(ZAHLUNG, kundenId, bestellnummer, UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("D-19: Zahlung einer anderen Bestellung ergibt 404")
        void zahlungAndererBestellung() throws Exception {
            String andere = gibAuf(null);

            setze(andere, ersteZahlung, "ERFOLGREICH").andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("D-19: Bestellung eines anderen Kunden ergibt 404")
        void fremdeBestellung() throws Exception {
            UUID fremder =
                    kundeService
                            .registriere("Bob", "Beispiel", "bob@example.de", HAUPTSTRASSE, false)
                            .getId();

            mockMvc.perform(
                            put(STATUS, fremder, bestellnummer, ersteZahlung)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"transaktionsstatus\": \"ERFOLGREICH\"}"))
                    .andExpect(status().isNotFound());
            mockMvc.perform(get(ZAHLUNG, fremder, bestellnummer, ersteZahlung))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ErstattungBuchen {

        @BeforeEach
        void bezahlt() throws Exception {
            setze(ersteZahlung, "ERFOLGREICH").andExpect(status().isOk());
        }

        @Test
        @DisplayName("D-19: Erstattung ergibt 201 mit Location, Status AUSSTEHEND")
        void wirdAngelegt() throws Exception {
            ResultActions antwort =
                    erstatte(3_000, "LASTSCHRIFT")
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.buchungsart").value("ERSTATTUNG"))
                            .andExpect(jsonPath("$.transaktionsstatus").value("AUSSTEHEND"))
                            .andExpect(jsonPath("$.zahlungsart").value("LASTSCHRIFT"))
                            .andExpect(jsonPath("$.betrag.cent").value(3_000))
                            .andExpect(jsonPath("$.zahlungszeitpunkt").value(nullValue()));
            String erstattung = id(antwort);
            antwort.andExpect(
                    header().string(
                                    "Location",
                                    endsWith(
                                            "/api/v1/kunden/%s/bestellungen/%s/zahlungen/%s"
                                                    .formatted(
                                                            kundenId, bestellnummer, erstattung))));

            mockMvc.perform(get(ZAHLUNG, kundenId, bestellnummer, erstattung))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.buchungsart").value("ERSTATTUNG"));
            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.bezahlteSumme.cent").value(10_000));
        }

        @Test
        @DisplayName("D-19: mehr als erstattbar ergibt 422, auch mit ausstehender Erstattung")
        void mehrAlsErstattbar() throws Exception {
            erstatte(10_001, "RECHNUNG")
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(
                            jsonPath("$.detail")
                                    .value(
                                            "Erstattung über 100,01 EUR übersteigt den erstattbaren Betrag von"
                                                    + " 100,00 EUR."));
            erstatte(7_000, "RECHNUNG").andExpect(status().isCreated());
            erstatte(3_001, "RECHNUNG").andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("D-19: Betrag 0, fehlende Zahlungsart oder fremde Währung ergeben 400")
        void ungueltigeEingabe() throws Exception {
            erstatte(0, "RECHNUNG").andExpect(status().isBadRequest());
            mockMvc.perform(
                            post(ERSTATTUNGEN, kundenId, bestellnummer)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"betrag\": {\"cent\": 100, \"waehrung\": \"EUR\"}}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fehler[0].feld").value("zahlungsart"));
            mockMvc.perform(
                            post(ERSTATTUNGEN, kundenId, bestellnummer)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                            {"zahlungsart": "RECHNUNG",
                                             "betrag": {"cent": 100, "waehrung": "USD"}}
                                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fehler[0].feld").value("betrag.waehrung"));
        }

        @Test
        @DisplayName("D-19: unbekannte Bestellung ergibt 404")
        void unbekannteBestellung() throws Exception {
            mockMvc.perform(
                            post(ERSTATTUNGEN, kundenId, "B-2026-999999")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(erstattungJson(100, "RECHNUNG")))
                    .andExpect(status().isNotFound());
        }
    }

    private String gibAuf(String gutscheincode) throws Exception {
        String code = gutscheincode == null ? "null" : "\"" + gutscheincode + "\"";
        return mockMvc.perform(
                        post(BESTELLUNGEN, kundenId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"positionen": [{"produktId": "%s", "menge": 1}],
                                         "lieferadresseId": "%s", "zahlungsart": "RECHNUNG",
                                         "gutscheincode": %s}
                                        """
                                                .formatted(artikel.getId(), lieferadresse, code)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location")
                .replaceAll(".*/", "");
    }

    private String ersteZahlung(String nummer) throws Exception {
        String antwort =
                mockMvc.perform(get(BESTELLUNG, kundenId, nummer))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return JsonPath.read(antwort, "$.zahlungen[0].id");
    }

    private ResultActions setze(String zahlungId, String status) throws Exception {
        return setze(bestellnummer, zahlungId, status);
    }

    private ResultActions setze(String nummer, String zahlungId, String status) throws Exception {
        return mockMvc.perform(
                put(STATUS, kundenId, nummer, zahlungId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transaktionsstatus\": \"%s\"}".formatted(status)));
    }

    private ResultActions erstatte(long cent, String zahlungsart) throws Exception {
        return mockMvc.perform(
                post(ERSTATTUNGEN, kundenId, bestellnummer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(erstattungJson(cent, zahlungsart)));
    }

    private static String erstattungJson(long cent, String zahlungsart) {
        return """
        {"zahlungsart": "%s", "betrag": {"cent": %d, "waehrung": "EUR"}}
        """
                .formatted(zahlungsart, cent);
    }

    private static String id(ResultActions antwort) throws Exception {
        return JsonPath.read(antwort.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
