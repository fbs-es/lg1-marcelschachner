package fbs.lg1.web.dto;

import fbs.lg1.Radiobeitrag;
import fbs.lg1.Werbebeitrag;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage für einen neuen Werbebeitrag ({@code "typ": "WERBUNG"}).
 *
 * @param titel der Titel
 * @param dauer die Dauer in Sekunden
 * @param kunde der Kunde
 * @param werbebudget das Werbebudget in Euro
 */
@Schema(description = "Neuer Werbebeitrag")
public record WerbebeitragRequest(
        @Schema(description = "Titel", example = "Kaffee-Spot") String titel,
        @Schema(description = "Dauer in Sekunden", example = "30") int dauer,
        @Schema(description = "Kunde", example = "Bohnenglück GmbH") String kunde,
        @Schema(description = "Werbebudget in Euro", example = "5000.0") double werbebudget)
        implements BeitragRequest {

    /**
     * Erstellt einen {@link Werbebeitrag} aus den Anfragedaten.
     */
    @Override
    public Radiobeitrag erstelleBeitrag() {
        return new Werbebeitrag(titel, dauer, kunde, werbebudget);
    }
}
