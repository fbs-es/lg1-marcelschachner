package de.bestellsystem.bestellung.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

/** US-04.1, US-04.2 und US-08.1 über HTTP; die feste Uhr steht auf dem 12.11.2026. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class BestellungApiIT {

    private static final String BESTELLUNGEN = "/api/v1/kunden/{kundenId}/bestellungen";
    private static final String BESTELLUNG = BESTELLUNGEN + "/{bestellnummer}";
    private static final Anschrift HAUPTSTRASSE =
            new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");
    private static final Anschrift NEBENSTRASSE =
            new Anschrift("Nebenstr. 5", "Berlin", "10115", "DE");

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcClient jdbc;
    @Autowired private ProduktRepository produkte;
    @Autowired private GutscheinRepository gutscheine;
    @Autowired private KundeService kundeService;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private Produkt buch;
    private Produkt lampe;
    private UUID kundenId;
    private UUID lieferadresse;

    @BeforeEach
    void beispieldaten() {
        Datenbestand.leere(jdbc);
        buch = produkte.save(new Produkt("Buch", Geld.ausCent(1250), 10));
        lampe = produkte.save(new Produkt("Lampe", Geld.ausCent(3000), 1));
        Kunde kunde =
                kundeService.registriere("Anna", "Muster", "anna@example.de", HAUPTSTRASSE, false);
        kundenId = kunde.getId();
        lieferadresse = kunde.getAdressen().getFirst().getId();
        gutscheine.save(
                Gutschein.prozent("HERBST10", new BigDecimal("10"), LocalDate.of(2026, 11, 30)));
        gutscheine.save(
                Gutschein.festbetrag("MINUS15", Geld.ausCent(1500), LocalDate.of(2026, 12, 31)));
        gutscheine.save(
                Gutschein.prozent("SOMMER20", new BigDecimal("20"), LocalDate.of(2026, 8, 31)));
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Nested
    class BestellungAufgeben {

        @Test
        @DisplayName("US-04.1 AK1: Bestellung wird mit Positionen, Fälligkeit und Zahlung angelegt")
        void wirdAngelegt() throws Exception {
            String bestellnummer =
                    gibAuf(zweiBuecherEineLampe(), null)
                            .andExpect(status().isCreated())
                            .andExpect(
                                    header().string(
                                                    "Location",
                                                    matchesPattern(
                                                            ".*/api/v1/kunden/"
                                                                    + kundenId
                                                                    + "/bestellungen/B-2026-\\d{6}")))
                            .andReturn()
                            .getResponse()
                            .getHeader("Location")
                            .replaceAll(".*/", "");

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bestellnummer").value(bestellnummer))
                    .andExpect(jsonPath("$.kundenId").value(kundenId.toString()))
                    .andExpect(jsonPath("$.bestelldatum").value("2026-11-12"))
                    .andExpect(jsonPath("$.positionen.length()").value(2))
                    .andExpect(jsonPath("$.positionen[0].zeilennummer").value(1))
                    .andExpect(jsonPath("$.positionen[0].titel").value("Buch"))
                    .andExpect(jsonPath("$.positionen[0].menge").value(2))
                    .andExpect(jsonPath("$.positionen[0].einzelpreis.cent").value(1250))
                    .andExpect(jsonPath("$.positionen[0].zwischenbetrag.cent").value(2500))
                    .andExpect(jsonPath("$.positionen[1].zeilennummer").value(2))
                    .andExpect(jsonPath("$.positionen[1].titel").value("Lampe"))
                    .andExpect(jsonPath("$.positionen[1].menge").value(1))
                    .andExpect(jsonPath("$.positionen[1].einzelpreis.cent").value(3000))
                    .andExpect(jsonPath("$.positionen[1].zwischenbetrag.cent").value(3000))
                    .andExpect(jsonPath("$.warenwert.cent").value(5500))
                    .andExpect(jsonPath("$.rabattBetrag.cent").value(0))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(5500))
                    .andExpect(jsonPath("$.gesamtwert.waehrung").value("EUR"))
                    .andExpect(jsonPath("$.faelligAm").value("2026-11-26"))
                    .andExpect(jsonPath("$.lieferanschrift.strasse").value("Hauptstr. 1"))
                    .andExpect(jsonPath("$.zahlungen.length()").value(1))
                    .andExpect(jsonPath("$.zahlungen[0].betrag.cent").value(5500))
                    .andExpect(jsonPath("$.zahlungen[0].zahlungsart").value("RECHNUNG"))
                    .andExpect(jsonPath("$.zahlungen[0].buchungsart").value("ZAHLUNG"))
                    .andExpect(jsonPath("$.zahlungen[0].transaktionsstatus").value("AUSSTEHEND"))
                    .andExpect(jsonPath("$.zahlungen[0].zahlungszeitpunkt").value(nullValue()));
        }

        @Test
        @DisplayName("US-04.1 AK1: jede Bestellung erhält eine eigene Bestellnummer")
        void eindeutigeBestellnummer() throws Exception {
            String erste = bestellnummer(gibAuf(einBuch(), null));
            String zweite = bestellnummer(gibAuf(einBuch(), null));

            assertThat(erste).isNotEqualTo(zweite);
        }

        @Test
        @DisplayName("US-04.1 AK2: Lagerbestand sinkt auf 8 Bücher und 0 Lampen")
        void bestandSinkt() throws Exception {
            gibAuf(zweiBuecherEineLampe(), null).andExpect(status().isCreated());

            assertThat(bestand(buch)).isEqualTo(8);
            assertThat(bestand(lampe)).isZero();
        }

        @Test
        @DisplayName("US-04.1 AK3: nicht genug Bestand ergibt 409 mit Lampe, nichts ändert sich")
        void nichtGenugBestand() throws Exception {
            String positionen = position(lampe, 2) + "," + position(buch, 1);

            gibAuf(positionen, null)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Konflikt"))
                    .andExpect(jsonPath("$.detail").value(containsString("Lampe")))
                    .andExpect(jsonPath("$.detail").value(not(containsString("Buch"))));

            assertThat(anzahlBestellungen()).isZero();
            assertThat(bestand(lampe)).isEqualTo(1);
            assertThat(bestand(buch)).isEqualTo(10);
        }

        @Test
        @DisplayName("US-04.1 AK4: Listenpreis steigt auf 15,00 €, Bestellung bleibt bei 12,50 €")
        void preisEingefroren() throws Exception {
            String bestellnummer = bestellnummer(gibAuf(zweiBuecherEineLampe(), null));

            // Listenpreis ändern gibt es noch nicht als Funktion (US-02.2).
            jdbc.sql("UPDATE produkt SET listenpreis_cent = 1500 WHERE id = ?")
                    .param(buch.getId())
                    .update();

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.positionen[0].einzelpreis.cent").value(1250))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(5500));
        }

        @Test
        @DisplayName(
                "US-04.1 AK5: Ändern oder Löschen der Adresse lässt die Lieferanschrift stehen")
        void lieferanschriftIstSnapshot() throws Exception {
            String bestellnummer = bestellnummer(gibAuf(zweiBuecherEineLampe(), null));

            kundeService.aendereAdresse(kundenId, lieferadresse, NEBENSTRASSE, true, false);
            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.lieferanschrift.strasse").value("Hauptstr. 1"))
                    .andExpect(jsonPath("$.lieferanschrift.postleitzahl").value("10115"))
                    .andExpect(jsonPath("$.lieferanschrift.stadt").value("Berlin"))
                    .andExpect(jsonPath("$.lieferanschrift.land").value("DE"));

            kundeService.fuegeAdresseHinzu(kundenId, NEBENSTRASSE, true, false);
            kundeService.entferneAdresse(kundenId, lieferadresse);
            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.lieferanschrift.strasse").value("Hauptstr. 1"));
        }

        @Test
        @DisplayName("US-04.1 AK6: Buch zweimal mit Menge 1 ergibt eine Position mit Menge 2")
        void duplikateZusammengefasst() throws Exception {
            gibAuf(position(buch, 1) + "," + position(buch, 1), null)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.positionen.length()").value(1))
                    .andExpect(jsonPath("$.positionen[0].titel").value("Buch"))
                    .andExpect(jsonPath("$.positionen[0].menge").value(2));
        }

        @Test
        @DisplayName("US-04.1 AK7: Bestellung ohne Positionen ergibt 400")
        void leereBestellung() throws Exception {
            gibAuf("", null)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("positionen")));

            assertThat(anzahlBestellungen()).isZero();
        }

        @Test
        @DisplayName("US-04.1 AK8: Adresse eines anderen Kunden ergibt 404")
        void fremdeAdresse() throws Exception {
            Kunde bernd =
                    kundeService.registriere(
                            "Bernd", "Beispiel", "bernd@example.de", NEBENSTRASSE, false);
            UUID fremde = bernd.getAdressen().getFirst().getId();

            gibAuf(kundenId, einBuch(), fremde, null)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Nicht gefunden"));

            assertThat(anzahlBestellungen()).isZero();
            assertThat(bestand(buch)).isEqualTo(10);
        }

        @Test
        @DisplayName("US-04.1 AK8: Adresse ohne Lieferkennzeichen ergibt 422")
        void keineLieferadresse() throws Exception {
            UUID rechnungsadresse =
                    kundeService.fuegeAdresseHinzu(kundenId, NEBENSTRASSE, false, true).getId();

            gibAuf(kundenId, einBuch(), rechnungsadresse, null)
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.detail").value(containsString("keine Lieferadresse")));

            assertThat(anzahlBestellungen()).isZero();
        }

        @Test
        @DisplayName("D-10: Menge 0 ergibt 400 mit Feldhinweis")
        void mengeNull() throws Exception {
            gibAuf(position(buch, 0), null)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("positionen[0].menge")));
        }

        @Test
        @DisplayName("D-17: unbekanntes Produkt ergibt 404")
        void unbekanntesProdukt() throws Exception {
            String positionen =
                    "{\"produktId\": \"%s\", \"menge\": 1}".formatted(UUID.randomUUID());

            gibAuf(positionen, null).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("D-17: unbekannter Kunde ergibt 404")
        void unbekannterKunde() throws Exception {
            gibAuf(UUID.randomUUID(), einBuch(), lieferadresse, null)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("D-17: fehlende Zahlungsart ergibt 400")
        void ohneZahlungsart() throws Exception {
            mockMvc.perform(
                            post(BESTELLUNGEN, kundenId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                            {"positionen": [%s], "lieferadresseId": "%s"}
                                            """
                                                    .formatted(einBuch(), lieferadresse)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("zahlungsart")));
        }

        @Test
        @DisplayName("D-17: Bestellung eines anderen Kunden ergibt 404")
        void fremdeBestellung() throws Exception {
            String bestellnummer = bestellnummer(gibAuf(einBuch(), null));

            mockMvc.perform(get(BESTELLUNG, UUID.randomUUID(), bestellnummer))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GutscheinEinloesen {

        @Test
        @DisplayName(
                "US-08.1 AK1: HERBST10 auf 55,00 € ergibt Rabatt 5,50 € und Gesamtwert 49,50 €")
        void prozentrabatt() throws Exception {
            gibAuf(zweiBuecherEineLampe(), "HERBST10")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.gutscheincode").value("HERBST10"))
                    .andExpect(jsonPath("$.warenwert.cent").value(5500))
                    .andExpect(jsonPath("$.rabattBetrag.cent").value(550))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(4950))
                    .andExpect(jsonPath("$.zahlungen[0].betrag.cent").value(4950));
        }

        @Test
        @DisplayName("US-08.1 AK2: MINUS15 auf 55,00 € ergibt Gesamtwert 40,00 €")
        void festbetrag() throws Exception {
            gibAuf(zweiBuecherEineLampe(), "MINUS15")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.gesamtwert.cent").value(4000));
        }

        @Test
        @DisplayName("US-08.1 AK3: MINUS15 auf 10,00 € ergibt Rabatt 10,00 € und Gesamtwert 0,00 €")
        void nieNegativ() throws Exception {
            Produkt heft = produkte.save(new Produkt("Heft", Geld.ausCent(1000), 5));

            gibAuf(position(heft, 1), "MINUS15")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rabattBetrag.cent").value(1000))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(0))
                    .andExpect(jsonPath("$.zahlungen[0].betrag.cent").value(0))
                    .andExpect(jsonPath("$.zahlungen[0].transaktionsstatus").value("AUSSTEHEND"));
        }

        @Test
        @DisplayName("US-08.1 AK4: SOMMER20 am 12.11.2026 ergibt 422 abgelaufen, ohne Code geht es")
        void abgelaufen() throws Exception {
            gibAuf(zweiBuecherEineLampe(), "SOMMER20")
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Fachlich abgelehnt"))
                    .andExpect(jsonPath("$.detail").value(containsString("abgelaufen")));
            assertThat(anzahlBestellungen()).isZero();
            assertThat(bestand(lampe)).isEqualTo(1);

            gibAuf(zweiBuecherEineLampe(), null)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.gutscheincode").value(nullValue()))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(5500));
        }

        @Test
        @DisplayName("US-08.1 AK6: herbst10 wendet HERBST10 an")
        void kleinschreibung() throws Exception {
            gibAuf(zweiBuecherEineLampe(), " herbst10 ")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.gutscheincode").value("HERBST10"))
                    .andExpect(jsonPath("$.rabattBetrag.cent").value(550));
        }

        @Test
        @DisplayName("US-08.1 AK6: GIBTSNICHT wird als unbekannt mit 422 abgelehnt")
        void unbekannt() throws Exception {
            gibAuf(zweiBuecherEineLampe(), "GIBTSNICHT")
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.detail").value(containsString("unbekannt")));

            assertThat(anzahlBestellungen()).isZero();
            assertThat(bestand(buch)).isEqualTo(10);
        }

        @Test
        @DisplayName("US-08.1 AK8: späteres Ändern oder Ablaufen des Gutscheins ändert nichts")
        void rabattEingefroren() throws Exception {
            String bestellnummer = bestellnummer(gibAuf(zweiBuecherEineLampe(), "HERBST10"));

            // Gutschein ändern gibt es noch nicht als Funktion (US-08.2).
            jdbc.sql(
                            "UPDATE gutschein SET prozent = 50, ablaufdatum = DATE '2026-01-01'"
                                    + " WHERE code = 'HERBST10'")
                    .update();

            mockMvc.perform(get(BESTELLUNG, kundenId, bestellnummer))
                    .andExpect(jsonPath("$.rabattBetrag.cent").value(550))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(4950));
        }

        @Test
        @DisplayName("US-08.1 AK9: HERBST10 auf 0,25 € ergibt 0,03 € Rabatt")
        void kaufmaennischGerundet() throws Exception {
            Produkt klammer = produkte.save(new Produkt("Klammer", Geld.ausCent(25), 5));

            gibAuf(position(klammer, 1), "HERBST10")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rabattBetrag.cent").value(3))
                    .andExpect(jsonPath("$.gesamtwert.cent").value(22));
        }
    }

    @Nested
    class Bestellhistorie {

        private String aelteste;
        private String mitGutschein;
        private String neueste;

        private void beispielbestellungen() throws Exception {
            aelteste = bestellnummer(gibAuf(einBuch(), null));
            // Bestellungen an anderen Tagen entstehen sonst nur mit einer zweiten Uhr.
            jdbc.sql(
                            "UPDATE bestellung SET bestelldatum = DATE '2026-11-10' WHERE bestellnummer = ?")
                    .param(aelteste)
                    .update();
            mitGutschein = bestellnummer(gibAuf(zweiBuecherEineLampe(), "HERBST10"));
            neueste = bestellnummer(gibAuf(einBuch(), null));
        }

        @Test
        @DisplayName(
                "US-04.2 AK1: Historie zeigt Bestellnummer, Datum, Positionsanzahl und Gesamtwert")
        void listeDerEigenenBestellungen() throws Exception {
            beispielbestellungen();

            mockMvc.perform(get(BESTELLUNGEN, kundenId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[1].bestellnummer").value(mitGutschein))
                    .andExpect(jsonPath("$[1].bestelldatum").value("2026-11-12"))
                    .andExpect(jsonPath("$[1].anzahlPositionen").value(2))
                    .andExpect(jsonPath("$[1].gesamtwert.cent").value(4950))
                    .andExpect(jsonPath("$[1].gesamtwert.waehrung").value("EUR"))
                    .andExpect(jsonPath("$[2].bestelldatum").value("2026-11-10"))
                    .andExpect(jsonPath("$[2].anzahlPositionen").value(1))
                    .andExpect(jsonPath("$[2].gesamtwert.cent").value(1250));
        }

        @Test
        @DisplayName("US-04.2 AK2: neueste zuerst, bei gleichem Datum höhere Bestellnummer zuerst")
        void neuesteZuerst() throws Exception {
            beispielbestellungen();

            mockMvc.perform(get(BESTELLUNGEN, kundenId))
                    .andExpect(jsonPath("$[0].bestellnummer").value(neueste))
                    .andExpect(jsonPath("$[1].bestellnummer").value(mitGutschein))
                    .andExpect(jsonPath("$[2].bestellnummer").value(aelteste));
        }

        @Test
        @DisplayName("US-04.2 AK3: Gesamtwert bleibt beim Ändern des Gutscheins eingefroren")
        void gesamtwertBerechnet() throws Exception {
            mitGutschein = bestellnummer(gibAuf(zweiBuecherEineLampe(), "HERBST10"));
            jdbc.sql("UPDATE gutschein SET prozent = 50 WHERE code = 'HERBST10'").update();

            mockMvc.perform(get(BESTELLUNGEN, kundenId))
                    .andExpect(jsonPath("$[0].bestellnummer").value(mitGutschein))
                    .andExpect(jsonPath("$[0].gesamtwert.cent").value(4950));
        }

        @Test
        @DisplayName("US-04.2 AK4: Bestellungen anderer Kunden erscheinen nicht")
        void nurEigene() throws Exception {
            Kunde bernd =
                    kundeService.registriere(
                            "Bernd", "Beispiel", "bernd@example.de", NEBENSTRASSE, false);
            gibAuf(bernd.getId(), einBuch(), bernd.getAdressen().getFirst().getId(), null)
                    .andExpect(status().isCreated());
            String eigene = bestellnummer(gibAuf(einBuch(), null));

            mockMvc.perform(get(BESTELLUNGEN, kundenId))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].bestellnummer").value(eigene));
        }

        @Test
        @DisplayName("US-04.2 AK5: ohne Bestellungen ist die Historie leer")
        void leer() throws Exception {
            mockMvc.perform(get(BESTELLUNGEN, kundenId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("US-04.2 AK6: Historie eines unbekannten Kunden ergibt 404")
        void unbekannterKunde() throws Exception {
            mockMvc.perform(get(BESTELLUNGEN, UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Nicht gefunden"));
        }

        @Test
        @DisplayName("US-04.2: Historie braucht unabhängig von der Bestellanzahl 2 Abfragen")
        void historieOhneNPlusEins() throws Exception {
            gibAuf(einBuch(), null).andExpect(status().isCreated());
            assertThat(abfragen(get(BESTELLUNGEN, kundenId))).isEqualTo(2);

            gibAuf(zweiBuecherEineLampe(), "HERBST10").andExpect(status().isCreated());
            gibAuf(einBuch(), null).andExpect(status().isCreated());
            assertThat(abfragen(get(BESTELLUNGEN, kundenId))).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Fahrplan 4a: Bestellabruf braucht unabhängig von der Positionszahl 2 Abfragen")
    void bestellabrufOhneNPlusEins() throws Exception {
        String klein = bestellnummer(gibAuf(einBuch(), "HERBST10"));
        StringBuilder viele = new StringBuilder(einBuch());
        for (int i = 0; i < 4; i++) {
            Produkt produkt = produkte.save(new Produkt("Artikel " + i, Geld.ausCent(100), 5));
            viele.append(",").append(position(produkt, 1));
        }
        String gross = bestellnummer(gibAuf(viele.toString(), "HERBST10"));

        assertThat(abfragenFuerAbruf(klein)).isEqualTo(2);
        assertThat(abfragenFuerAbruf(gross)).isEqualTo(2);
    }

    private long abfragenFuerAbruf(String bestellnummer) throws Exception {
        return abfragen(get(BESTELLUNG, kundenId, bestellnummer));
    }

    private long abfragen(RequestBuilder anfrage) throws Exception {
        Statistics statistik = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistik.setStatisticsEnabled(true);
        statistik.clear();
        try {
            mockMvc.perform(anfrage).andExpect(status().isOk());
            return statistik.getPrepareStatementCount();
        } finally {
            statistik.setStatisticsEnabled(false);
        }
    }

    private ResultActions gibAuf(String positionen, String gutscheincode) throws Exception {
        return gibAuf(kundenId, positionen, lieferadresse, gutscheincode);
    }

    private ResultActions gibAuf(UUID kunde, String positionen, UUID adresse, String gutscheincode)
            throws Exception {
        String code = gutscheincode == null ? "null" : "\"" + gutscheincode + "\"";
        return mockMvc.perform(
                post(BESTELLUNGEN, kunde)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"positionen": [%s], "lieferadresseId": "%s",
                                 "zahlungsart": "RECHNUNG", "gutscheincode": %s}
                                """
                                        .formatted(positionen, adresse, code)));
    }

    private static String bestellnummer(ResultActions antwort) throws Exception {
        return antwort.andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location")
                .replaceAll(".*/", "");
    }

    private String zweiBuecherEineLampe() {
        return position(buch, 2) + "," + position(lampe, 1);
    }

    private String einBuch() {
        return position(buch, 1);
    }

    private static String position(Produkt produkt, int menge) {
        return "{\"produktId\": \"%s\", \"menge\": %d}".formatted(produkt.getId(), menge);
    }

    private int bestand(Produkt produkt) {
        return produkte.findById(produkt.getId()).orElseThrow().getLagerbestand();
    }

    private long anzahlBestellungen() {
        return jdbc.sql("SELECT count(*) FROM bestellung").query(Long.class).single();
    }
}
