package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Antwort nach dem Einfügen einer Eilmeldung.
 *
 * @param sendeplan der neue Sendeplan
 * @param warnung die Warnung an den Moderator bei Überschreitung des Zeitlimits, sonst null
 */
@Schema(description = "Ergebnis des Einfügens einer Eilmeldung")
public record EilmeldungErgebnisDto(
        @Schema(description = "Neuer Sendeplan") SendeplanDto sendeplan,
        @Schema(
                        description = "Warnung bei Überschreitung des Zeitlimits, sonst null",
                        nullable = true,
                        example =
                                "WARNUNG an Marcel Schachner: Sendung 'Morgenshow' überschreitet"
                                        + " durch die Eilmeldung ihr Zeitlimit um 30 Sekunden!")
                String warnung) {
}
