package de.bestellsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class BestellsystemApplicationIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private Clock uhr;
    @Autowired private FachlicheEinstellungen einstellungen;

    @Test
    @DisplayName("E00: Ein Test kann Datum und Uhrzeit festlegen")
    void uhrIstImTestFestgelegt() {
        assertThat(uhr.instant()).isEqualTo(FesteUhrKonfiguration.JETZT);
        assertThat(LocalDate.now(uhr)).isEqualTo(LocalDate.of(2026, 11, 12));
    }

    @Test
    @DisplayName("D-03: Zahlungsziel 14 Tage, Mahnintervall 7 Tage")
    void fachlicheStandardwerte() {
        assertThat(einstellungen.zahlungsziel()).isEqualTo(Period.ofDays(14));
        assertThat(einstellungen.mahnintervall()).isEqualTo(Period.ofDays(7));
    }

    @Test
    void openApiIstErreichbar() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Bestellsystem API"));
    }

    @Test
    void swaggerUiIstErreichbar() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
