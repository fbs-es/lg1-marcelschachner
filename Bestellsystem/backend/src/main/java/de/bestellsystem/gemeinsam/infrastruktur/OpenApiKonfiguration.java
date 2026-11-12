package de.bestellsystem.gemeinsam.infrastruktur;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OpenApiKonfiguration {

    @Bean
    OpenAPI bestellsystemOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Bestellsystem API")
                                .version("v1")
                                .description(
                                        "REST-API des Bestellsystems. Basis /api/v1. Fehler als"
                                                + " ProblemDetail (RFC 9457), Geldbeträge in Cent"
                                                + " (EUR)."))
                // Feste relative Server-URL statt des Request-Hosts: die exportierte Spec
                // (frontend/openapi.json) bleibt dadurch unabhängig vom Port reproduzierbar.
                .servers(List.of(new Server().url("/").description("Aktueller Host")));
    }
}
