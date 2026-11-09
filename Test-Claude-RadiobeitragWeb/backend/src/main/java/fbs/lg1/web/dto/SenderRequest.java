package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Anfrage zum Anlegen eines Radiosenders samt seinem Tagesprogramm.
 *
 * @param name der Name des Senders
 * @param frequenz die Übertragungsfrequenz in MHz
 * @param datum der Tag des Programms
 */
@Schema(description = "Daten für einen neuen Radiosender mit Programm")
public record SenderRequest(
        @Schema(description = "Name des Senders", example = "Radio AirControl") String name,
        @Schema(description = "Übertragungsfrequenz in MHz", example = "98.5") double frequenz,
        @Schema(description = "Tag des Programms", example = "2026-11-09") LocalDate datum) {
}
