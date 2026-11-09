package fbs.lg1.web.dto;

import fbs.lg1.Musikgenre;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Antwort mit einem Beitrag im Sendeplan. Die typabhängigen Felder sind nur beim passenden Typ
 * gesetzt, sonst null.
 *
 * @param id die ID des Beitrags
 * @param typ die Beitragsart: MUSIK, WERBUNG, MODERATION oder EILMELDUNG
 * @param titel der Titel des Beitrags
 * @param dauer die Dauer in Sekunden
 * @param startzeitpunkt der Startzeitpunkt in Sekunden ab Sendungsbeginn
 * @param kuenstler der Künstler bei Musik
 * @param album das Album bei Musik
 * @param genre das Genre bei Musik
 * @param kunde der Kunde bei Werbung
 * @param werbebudget das Werbebudget in Euro bei Werbung
 * @param sprechertext der Sprechertext bei Moderation
 * @param dringlichkeitsstufe die Dringlichkeitsstufe bei Eilmeldung
 * @param region die Region bei Eilmeldung
 */
@Schema(description = "Ein Beitrag im Sendeplan")
public record BeitragDto(
        @Schema(description = "ID des Beitrags", example = "1") Long id,
        @Schema(
                        description = "Beitragsart",
                        allowableValues = {"MUSIK", "WERBUNG", "MODERATION", "EILMELDUNG"})
                String typ,
        @Schema(description = "Titel des Beitrags", example = "Wolkenlos") String titel,
        @Schema(description = "Dauer in Sekunden", example = "240") int dauer,
        @Schema(description = "Startzeitpunkt in Sekunden ab Sendungsbeginn", example = "60")
                int startzeitpunkt,
        @Schema(description = "Künstler (nur MUSIK)", nullable = true) String kuenstler,
        @Schema(description = "Album (nur MUSIK)", nullable = true) String album,
        @Schema(description = "Genre (nur MUSIK)", nullable = true) Musikgenre genre,
        @Schema(description = "Kunde (nur WERBUNG)", nullable = true) String kunde,
        @Schema(description = "Werbebudget in Euro (nur WERBUNG)", nullable = true) Double werbebudget,
        @Schema(description = "Sprechertext (nur MODERATION)", nullable = true) String sprechertext,
        @Schema(description = "Dringlichkeitsstufe 1 bis 5 (nur EILMELDUNG)", nullable = true)
                Integer dringlichkeitsstufe,
        @Schema(description = "Region (nur EILMELDUNG)", nullable = true) String region) {
}
