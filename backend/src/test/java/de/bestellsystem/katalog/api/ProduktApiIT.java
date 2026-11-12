package de.bestellsystem.katalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersKonfiguration.class)
class ProduktApiIT {

    private static final String PRODUKTE = "/api/v1/produkte";

    @Autowired private MockMvc mockMvc;
    @Autowired private ProduktRepository repository;

    @BeforeEach
    void leereKatalog() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("US-02.1 AK1: Produkt mit Lagerbestand 3 wird als verfügbar ausgeliefert")
    void verfuegbarBeiBestand() throws Exception {
        String id = legeAn("Stehlampe", 4999, 3);

        mockMvc.perform(get(PRODUKTE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titel").value("Stehlampe"))
                .andExpect(jsonPath("$.listenpreis.cent").value(4999))
                .andExpect(jsonPath("$.listenpreis.waehrung").value("EUR"))
                .andExpect(jsonPath("$.lagerbestand").value(3))
                .andExpect(jsonPath("$.istVerfuegbar").value(true));
    }

    @Test
    @DisplayName("US-02.1 AK2: Produkt mit Lagerbestand 0 wird als nicht verfügbar ausgeliefert")
    void nichtVerfuegbarOhneBestand() throws Exception {
        String id = legeAn("Stehlampe", 4999, 0);

        mockMvc.perform(get(PRODUKTE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.istVerfuegbar").value(false));
    }

    @Test
    @DisplayName("US-02.1 AK3: Bestand 3 reicht für Menge 3, nicht für Menge 4")
    void verfuegbarkeitFuerMenge() throws Exception {
        String id = legeAn("Stehlampe", 4999, 3);

        mockMvc.perform(get(PRODUKTE + "/{id}/verfuegbarkeit", id).param("menge", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produktId").value(id))
                .andExpect(jsonPath("$.menge").value(3))
                .andExpect(jsonPath("$.istVerfuegbar").value(true));
        mockMvc.perform(get(PRODUKTE + "/{id}/verfuegbarkeit", id).param("menge", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.istVerfuegbar").value(false));
    }

    @Test
    @DisplayName("US-02.1 AK4: Verfügbarkeit für Menge 0 oder negativ ergibt 400")
    void ungueltigeMenge() throws Exception {
        String id = legeAn("Stehlampe", 4999, 3);

        for (String menge : new String[] {"0", "-1"}) {
            mockMvc.perform(get(PRODUKTE + "/{id}/verfuegbarkeit", id).param("menge", menge))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Ungültige Eingabe"));
        }
    }

    @Test
    @DisplayName("US-02.1 AK5: Lagerbestand −1 setzen ergibt 400, Bestand bleibt")
    void bestandNieNegativ() throws Exception {
        String id = legeAn("Stehlampe", 4999, 3);

        mockMvc.perform(
                        put(PRODUKTE + "/{id}/lagerbestand", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"lagerbestand\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ungültige Eingabe"));

        assertThat(repository.findById(UUID.fromString(id)).orElseThrow().getLagerbestand())
                .isEqualTo(3);
    }

    @Test
    void lagerbestandSetzen() throws Exception {
        String id = legeAn("Stehlampe", 4999, 0);

        mockMvc.perform(
                        put(PRODUKTE + "/{id}/lagerbestand", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"lagerbestand\": 7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lagerbestand").value(7))
                .andExpect(jsonPath("$.istVerfuegbar").value(true));
    }

    @Test
    @DisplayName("D-15: Anlegen liefert 201 mit Location, Titel getrimmt")
    void anlegenLiefert201MitLocation() throws Exception {
        String id = legeAn("  Tischlampe ", 2500, 2);

        mockMvc.perform(get(PRODUKTE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titel").value("Tischlampe"))
                .andExpect(jsonPath("$.listenpreis.cent").value(2500));
    }

    @Test
    void listeIstNachTitelSortiert() throws Exception {
        legeAn("Zebra-Teppich", 100, 1);
        legeAn("Ahornregal", 200, 0);

        mockMvc.perform(get(PRODUKTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titel").value("Ahornregal"))
                .andExpect(jsonPath("$[0].istVerfuegbar").value(false))
                .andExpect(jsonPath("$[1].titel").value("Zebra-Teppich"));
    }

    @Test
    @DisplayName("D-15: leerer Titel, negativer Preis oder Bestand ergeben 400 mit Feldfehlern")
    void ungueltigesProduktWirdAbgelehnt() throws Exception {
        mockMvc.perform(
                        post(PRODUKTE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Accept-Language", "en-US")
                                .content(anlegenJson(" ", -1, -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ungültige Eingabe"))
                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("titel")))
                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("listenpreis.cent")))
                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("lagerbestand")))
                .andExpect(
                        jsonPath("$.fehler[?(@.feld == 'titel')].meldung")
                                .value(hasItem("darf nicht leer sein")));
    }

    @Test
    @DisplayName("D-11: andere Währung als EUR ergibt 400")
    void nurEuro() throws Exception {
        mockMvc.perform(
                        post(PRODUKTE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"titel": "Lampe",
                                         "listenpreis": {"cent": 100, "waehrung": "USD"},
                                         "lagerbestand": 1}
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("listenpreis.waehrung")));
    }

    @Test
    void unbekanntesProduktErgibt404() throws Exception {
        String unbekannt = UUID.randomUUID().toString();

        mockMvc.perform(get(PRODUKTE + "/{id}", unbekannt))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Nicht gefunden"));
        mockMvc.perform(get(PRODUKTE + "/{id}/verfuegbarkeit", unbekannt).param("menge", "1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(
                        put(PRODUKTE + "/{id}/lagerbestand", unbekannt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"lagerbestand\": 1}"))
                .andExpect(status().isNotFound());
    }

    private String legeAn(String titel, long cent, int lagerbestand) throws Exception {
        ResultActions antwort =
                mockMvc.perform(
                                post(PRODUKTE)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(anlegenJson(titel, cent, lagerbestand)))
                        .andExpect(status().isCreated());
        String id = JsonPath.read(antwort.andReturn().getResponse().getContentAsString(), "$.id");
        antwort.andExpect(header().string("Location", endsWith(PRODUKTE + "/" + id)));
        return id;
    }

    private static String anlegenJson(String titel, long cent, int lagerbestand) {
        return """
        {"titel": "%s", "listenpreis": {"cent": %d, "waehrung": "EUR"}, "lagerbestand": %d}
        """
                .formatted(titel, cent, lagerbestand);
    }
}
