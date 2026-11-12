package de.bestellsystem.gemeinsam.infrastruktur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.bestellsystem.TestcontainersKonfiguration;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Hält {@code frontend/openapi.json} synchron zum Code (ADR 0002). Neu schreiben mit {@code
 * ./gradlew aktualisiereOpenApi}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersKonfiguration.class)
class OpenApiSpezifikationIT {

    static final String AKTUALISIEREN = "openapi.aktualisieren";
    private static final Path DATEI = Path.of("..", "frontend", "openapi.json");

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("ADR 0002: frontend/openapi.json entspricht der aktuellen API")
    void exportierteSpezifikationIstAktuell() throws Exception {
        String roh =
                mockMvc.perform(get("/v3/api-docs"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String aktuell = json.writeValueAsString(json.readTree(roh)) + "\n";

        if (Boolean.getBoolean(AKTUALISIEREN)) {
            Files.writeString(DATEI, aktuell, StandardCharsets.UTF_8);
            return;
        }
        assertThat(DATEI)
                .as("frontend/openapi.json veraltet: ./gradlew aktualisiereOpenApi ausführen")
                .exists();
        assertThat(Files.readString(DATEI, StandardCharsets.UTF_8))
                .as("frontend/openapi.json veraltet: ./gradlew aktualisiereOpenApi ausführen")
                .isEqualTo(aktuell);
    }
}
