package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * Antwort mit einem Tagesprogramm und seinen Sendungen.
 *
 * @param id die ID des Programms
 * @param datum der Tag des Programms
 * @param sendungen die Sendungen nach Startzeit sortiert
 */
@Schema(description = "Ein Tagesprogramm mit seinen Sendungen")
public record ProgrammDto(
        @Schema(description = "ID des Programms", example = "1") Long id,
        @Schema(description = "Tag des Programms", example = "2026-11-09") LocalDate datum,
        @Schema(description = "Sendungen nach Startzeit sortiert") List<SendungDto> sendungen) {
}
