package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

/**
 * Anfrage zum Anlegen einer Sendung im Programm.
 *
 * @param name der Name der Sendung
 * @param moderator der Name des Moderators
 * @param startzeit der Beginn des Zeitblocks
 * @param endzeit das Ende des Zeitblocks
 * @param maximaldauer die erlaubte Sendezeit in Sekunden
 */
@Schema(description = "Daten für eine neue Sendung")
public record SendungRequest(
        @Schema(description = "Name der Sendung", example = "Morgenshow") String name,
        @Schema(description = "Name des Moderators", example = "Marcel Schachner") String moderator,
        @Schema(description = "Beginn des Zeitblocks", example = "08:00:00", type = "string")
                LocalTime startzeit,
        @Schema(description = "Ende des Zeitblocks", example = "09:00:00", type = "string")
                LocalTime endzeit,
        @Schema(description = "Erlaubte Sendezeit in Sekunden", example = "900") int maximaldauer) {
}
