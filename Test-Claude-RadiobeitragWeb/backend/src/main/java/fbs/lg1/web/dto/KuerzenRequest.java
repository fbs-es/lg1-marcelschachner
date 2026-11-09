package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage zum Kürzen eines Beitrags.
 *
 * @param neueDauer die neue, kürzere Dauer in Sekunden
 */
@Schema(description = "Neue Dauer eines Beitrags")
public record KuerzenRequest(
        @Schema(description = "Neue Dauer in Sekunden", example = "180") int neueDauer) {
}
