package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Fehlerantwort mit HTTP-Status und deutscher Meldung aus der Domäne.
 *
 * @param status der HTTP-Statuscode
 * @param message die Fehlermeldung
 */
@Schema(description = "Fehlerantwort")
public record FehlerDto(
        @Schema(description = "HTTP-Statuscode", example = "409") int status,
        @Schema(description = "Fehlermeldung", example = "Beitrag überschreitet die Maximaldauer der Sendung!")
                String message) {
}
