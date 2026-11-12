package de.bestellsystem.gemeinsam.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FehlerAusloesenderController.class)
class ZentraleFehlerbehandlungTest {

    @Autowired private MockMvc mockMvc;

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
        "nicht-gefunden,   404, Nicht gefunden,     Kunde 42 existiert nicht.",
        "konflikt,         409, Konflikt,           E-Mail-Adresse ist bereits vergeben.",
        "abgelehnt,        422, Fachlich abgelehnt, Gutschein ist abgelaufen.",
        "ungueltig,        400, Ungültige Eingabe,  land muss ein ISO-3166-Alpha-2-Code sein"
    })
    void uebersetztFachlicheFehlerInProblemDetail(
            String pfad, int status, String titel, String detail) throws Exception {
        mockMvc.perform(get("/test/fehler/" + pfad))
                .andExpect(status().is(status))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").value(titel))
                .andExpect(jsonPath("$.detail").value(detail))
                .andExpect(jsonPath("$.instance").value("/test/fehler/" + pfad));
    }

    @Test
    void versionskonfliktErgibt409() throws Exception {
        mockMvc.perform(get("/test/fehler/versionskonflikt"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Versionskonflikt"));
    }

    @Test
    void unerwarteterFehlerVerraetKeineInterna() throws Exception {
        mockMvc.perform(get("/test/fehler/unerwartet"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Interner Fehler"))
                .andExpect(content().string(not(containsString("geheimes"))));
    }

    @Test
    void validierungsfehlerNenntBetroffeneFelder() throws Exception {
        mockMvc.perform(
                        post("/test/fehler/validierung")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \" \", \"menge\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Ungültige Eingabe"))
                .andExpect(jsonPath("$.fehler[*].feld").value(containsInAnyOrder("name", "menge")));
    }

    @Test
    void unlesbaresJsonErgibt400AlsProblemDetail() throws Exception {
        mockMvc.perform(
                        post("/test/fehler/validierung")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{kaputt"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unbekannterPfadErgibt404AlsProblemDetail() throws Exception {
        mockMvc.perform(get("/gibt-es-nicht"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
