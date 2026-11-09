package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Antwort mit den Daten eines Radiosenders und seinem Programm.
 *
 * @param id die ID des Senders
 * @param name der Name des Senders
 * @param frequenz die Übertragungsfrequenz in MHz
 * @param programmId die ID des Programms
 * @param datum der Tag des Programms
 */
@Schema(description = "Ein Radiosender mit seinem Programm")
public record SenderDto(
        @Schema(description = "ID des Senders", example = "1") Long id,
        @Schema(description = "Name des Senders", example = "Radio AirControl") String name,
        @Schema(description = "Übertragungsfrequenz in MHz", example = "98.5") double frequenz,
        @Schema(description = "ID des Programms", example = "1") Long programmId,
        @Schema(description = "Tag des Programms", example = "2026-11-09") LocalDate datum) {
}
