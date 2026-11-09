package fbs.lg1.web.dto;

import fbs.lg1.Moderation;
import fbs.lg1.Radiobeitrag;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage für eine neue Moderation ({@code "typ": "MODERATION"}).
 *
 * @param titel der Titel
 * @param dauer die Dauer in Sekunden
 * @param sprechertext der Sprechertext
 */
@Schema(description = "Neue Moderation")
public record ModerationRequest(
        @Schema(description = "Titel", example = "Begrüßung") String titel,
        @Schema(description = "Dauer in Sekunden", example = "60") int dauer,
        @Schema(description = "Sprechertext", example = "Guten Morgen!") String sprechertext)
        implements BeitragRequest {

    /**
     * Erstellt eine {@link Moderation} aus den Anfragedaten.
     */
    @Override
    public Radiobeitrag erstelleBeitrag() {
        return new Moderation(titel, dauer, sprechertext);
    }
}
