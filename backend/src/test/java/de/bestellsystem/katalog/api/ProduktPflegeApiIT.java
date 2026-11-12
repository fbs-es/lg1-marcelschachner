package de.bestellsystem.katalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** US-02.2 über HTTP (D-10, D-15, D-23). */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class ProduktPflegeApiIT {

    private static final String PRODUKT = "/api/v1/produkte/{id}";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcClient jdbc;
    @Autowired private ProduktRepository produkte;
    @Autowired private ProduktService produktService;
    @Autowired private KundeService kundeService;
    @Autowired private PlatformTransactionManager transaktionsmanager;

    private Produkt lampe;

    @BeforeEach
    void beispieldaten() {
        Datenbestand.leere(jdbc);
        lampe = produkte.save(new Produkt("Stehlampe", Geld.ausCent(4999), 3));
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("US-02.2 AK1: Titel und Preis ändern liefert 200 mit den neuen Werten")
    void aendern() throws Exception {
        aendere(lampe.getId(), "Stehlampe Nova", 5999)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titel").value("Stehlampe Nova"))
                .andExpect(jsonPath("$.listenpreis.cent").value(5999))
                .andExpect(jsonPath("$.lagerbestand").value(3));

        mockMvc.perform(get(PRODUKT, lampe.getId()))
                .andExpect(jsonPath("$.titel").value("Stehlampe Nova"))
                .andExpect(jsonPath("$.listenpreis.cent").value(5999));
    }

    @Test
    @DisplayName(
            "US-02.2 AK2: leerer Titel, zu langer Titel oder negativer Preis: 400, unverändert")
    void ungueltigeAenderung() throws Exception {
        aendere(lampe.getId(), " ", 5999)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ungültige Eingabe"));
        aendere(lampe.getId(), "x".repeat(201), 5999).andExpect(status().isBadRequest());
        aendere(lampe.getId(), "Stehlampe", -1).andExpect(status().isBadRequest());

        Produkt gespeichert = produkte.findById(lampe.getId()).orElseThrow();
        assertThat(gespeichert.getTitel()).isEqualTo("Stehlampe");
        assertThat(gespeichert.getListenpreis()).isEqualTo(Geld.ausCent(4999));
    }

    @Test
    @DisplayName("US-02.2 AK3: unbekanntes Produkt ändern ergibt 404")
    void unbekanntesProdukt() throws Exception {
        aendere(UUID.randomUUID(), "Lampe", 100)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Nicht gefunden"));
    }

    @Test
    @DisplayName("US-02.2 AK4: Preiserhöhung ändert den Einzelpreis bestehender Bestellungen nicht")
    void bestehendeBestellungBleibt() throws Exception {
        Kunde kunde =
                kundeService.registriere(
                        "Anna",
                        "Muster",
                        "anna@example.de",
                        new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                        false);
        String ort =
                mockMvc.perform(
                                post("/api/v1/kunden/{kundenId}/bestellungen", kunde.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"positionen": [{"produktId": "%s", "menge": 1}],
                                                 "lieferadresseId": "%s",
                                                 "zahlungsart": "RECHNUNG"}
                                                """
                                                        .formatted(
                                                                lampe.getId(),
                                                                kunde.getAdressen()
                                                                        .getFirst()
                                                                        .getId())))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getHeader("Location");

        aendere(lampe.getId(), "Stehlampe", 5999).andExpect(status().isOk());

        mockMvc.perform(get(ort))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positionen[0].einzelpreis.cent").value(4999))
                .andExpect(jsonPath("$.gesamtwert.cent").value(4999));
    }

    @Test
    @DisplayName("US-02.2 AK5: Merkliste zeigt nach der Preiserhöhung den neuen Preis")
    void merklisteZeigtNeuenPreis() throws Exception {
        Kunde kunde =
                kundeService.registriere(
                        "Ben",
                        "Beispiel",
                        "ben@example.de",
                        new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                        false);
        mockMvc.perform(
                        put(
                                "/api/v1/kunden/{kundenId}/merkliste/{produktId}",
                                kunde.getId(),
                                lampe.getId()))
                .andExpect(status().isCreated());

        aendere(lampe.getId(), "Stehlampe", 5999).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/kunden/{kundenId}/merkliste", kunde.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].listenpreis.cent").value(5999));
    }

    @Test
    @DisplayName(
            "US-02.2 AK6: zwei gleichzeitige Änderungen, die zweite scheitert am Versionskonflikt")
    void gleichzeitigeAenderung() {
        TransactionTemplate erste = new TransactionTemplate(transaktionsmanager);
        TransactionTemplate zweite = new TransactionTemplate(transaktionsmanager);
        zweite.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Die erste liest, die zweite ändert und schreibt fest, dann schreibt die erste fest.
        assertThatThrownBy(() ->
                        erste.executeWithoutResult(status -> {
                            produktService.aendere(
                                    lampe.getId(), "Stehlampe A", Geld.ausCent(5000));
                            zweite.executeWithoutResult(innen ->
                                    produktService.aendere(
                                            lampe.getId(), "Stehlampe B", Geld.ausCent(6000)));
                        }))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        Produkt gespeichert = produkte.findById(lampe.getId()).orElseThrow();
        assertThat(gespeichert.getTitel()).isEqualTo("Stehlampe B");
        assertThat(gespeichert.getListenpreis()).isEqualTo(Geld.ausCent(6000));
    }

    private ResultActions aendere(UUID produktId, String titel, long cent) throws Exception {
        return mockMvc.perform(
                put(PRODUKT, produktId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"titel": "%s", "listenpreis": {"cent": %d, "waehrung": "EUR"}}
                                """
                                        .formatted(titel, cent)));
    }
}
