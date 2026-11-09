package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage zum Verschieben eines Beitrags.
 *
 * @param neuerIndex die neue Position im Sendeplan, beginnend bei 0
 */
@Schema(description = "Neue Position eines Beitrags")
public record VerschiebenRequest(
        @Schema(description = "Neue Position, beginnend bei 0", example = "0") int neuerIndex) {
}
