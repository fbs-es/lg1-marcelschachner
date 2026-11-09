package fbs.lg1.web.dto;

import fbs.lg1.Eilmeldung;
import fbs.lg1.Radiobeitrag;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage für eine Eilmeldung, die normal an das Ende des Sendeplans angehängt wird ({@code
 * "typ": "EILMELDUNG"}).
 *
 * @param titel der Titel
 * @param dauer die Dauer in Sekunden
 * @param dringlichkeitsstufe die Dringlichkeitsstufe von 1 bis 5
 * @param region die betroffene Region
 */
@Schema(description = "Neue Eilmeldung am Ende des Sendeplans")
public record EilmeldungRequest(
        @Schema(description = "Titel", example = "Sturmwarnung") String titel,
        @Schema(description = "Dauer in Sekunden", example = "45") int dauer,
        @Schema(description = "Dringlichkeitsstufe 1 bis 5", example = "4") int dringlichkeitsstufe,
        @Schema(description = "Betroffene Region", example = "Tirol") String region)
        implements BeitragRequest {

    /**
     * Erstellt eine {@link Eilmeldung} aus den Anfragedaten.
     */
    @Override
    public Radiobeitrag erstelleBeitrag() {
        return new Eilmeldung(titel, dauer, dringlichkeitsstufe, region);
    }
}
