package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;

/**
 * Antwort mit einer Sendung und ihrem vollständigen Sendeplan.
 *
 * @param id die ID der Sendung
 * @param name der Name der Sendung
 * @param moderator der Name des Moderators
 * @param startzeit der Beginn des Zeitblocks
 * @param endzeit das Ende des Zeitblocks
 * @param maximaldauer die erlaubte Sendezeit in Sekunden
 * @param gesamtdauer die verplante Zeit in Sekunden
 * @param restzeit die verbleibende Zeit in Sekunden, negativ bei Überschreitung
 * @param zeitlimitUeberschritten true, wenn die Maximaldauer überschritten ist
 * @param beitraege die Beiträge in Sendeplan-Reihenfolge
 */
@Schema(description = "Eine Sendung mit ihrem Sendeplan")
public record SendeplanDto(
        @Schema(description = "ID der Sendung", example = "1") Long id,
        @Schema(description = "Name der Sendung", example = "Morgenshow") String name,
        @Schema(description = "Name des Moderators", example = "Marcel Schachner") String moderator,
        @Schema(description = "Beginn des Zeitblocks", example = "08:00:00", type = "string")
                LocalTime startzeit,
        @Schema(description = "Ende des Zeitblocks", example = "09:00:00", type = "string")
                LocalTime endzeit,
        @Schema(description = "Erlaubte Sendezeit in Sekunden", example = "900") int maximaldauer,
        @Schema(description = "Verplante Zeit in Sekunden", example = "330") int gesamtdauer,
        @Schema(description = "Verbleibende Zeit in Sekunden", example = "570") int restzeit,
        @Schema(description = "Maximaldauer überschritten") boolean zeitlimitUeberschritten,
        @Schema(description = "Beiträge in Sendeplan-Reihenfolge") List<BeitragDto> beitraege) {
}
