package fbs.lg1.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beschreibt die REST-API für Swagger UI und das OpenAPI-Dokument.
 */
@Configuration
public class OpenApiConfig {

    /**
     * @return die allgemeinen Angaben zur API
     */
    @Bean
    public OpenAPI radiobeitragOpenApi() {
        Info info = new Info();
        info.title("Test-Claude-Radiobeitrag API");
        info.version("1.0");
        info.description("REST-API zur Sendeplanung von Radiosendern, Programmen und Sendungen.");
        OpenAPI openApi = new OpenAPI();
        openApi.info(info);
        return openApi;
    }
}
