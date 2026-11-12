package de.bestellsystem.gemeinsam.infrastruktur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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
 * Hält {@code frontend/openapi.json} und {@code docs/api/openapi.yaml} synchron zum Code (ADR
 * 0002). Neu schreiben mit {@code ./gradlew aktualisiereOpenApi}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersKonfiguration.class)
class OpenApiSpezifikationIT {

    static final String AKTUALISIEREN = "openapi.aktualisieren";
    private static final Path JSON_DATEI = Path.of("..", "frontend", "openapi.json");
    private static final Path YAML_DATEI = Path.of("..", "docs", "api", "openapi.yaml");

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("ADR 0002: frontend/openapi.json entspricht der aktuellen API")
    void exportierteSpezifikationIstAktuell() throws Exception {
        ObjectMapper json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        pruefeOderSchreibe(JSON_DATEI, json.writeValueAsString(aktuelleSpezifikation()) + "\n");
    }

    @Test
    @DisplayName("ADR 0002: docs/api/openapi.yaml entspricht der aktuellen API")
    void dokumentierteSpezifikationIstAktuell() throws Exception {
        YAMLMapper yaml =
                YAMLMapper.builder()
                        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                        .build();
        pruefeOderSchreibe(YAML_DATEI, yaml.writeValueAsString(aktuelleSpezifikation()));
    }

    private JsonNode aktuelleSpezifikation() throws Exception {
        String roh =
                mockMvc.perform(get("/v3/api-docs"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);
        return new ObjectMapper().readTree(roh);
    }

    private static void pruefeOderSchreibe(Path datei, String aktuell) throws Exception {
        if (Boolean.getBoolean(AKTUALISIEREN)) {
            Files.createDirectories(datei.getParent());
            Files.writeString(datei, aktuell, StandardCharsets.UTF_8);
            return;
        }
        String hinweis = datei.normalize() + " veraltet: ./gradlew aktualisiereOpenApi ausführen";
        assertThat(datei).as(hinweis).exists();
        assertThat(Files.readString(datei, StandardCharsets.UTF_8)).as(hinweis).isEqualTo(aktuell);
    }
}
