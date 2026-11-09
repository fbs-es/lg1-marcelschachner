package fbs.lg1.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage zum sofortigen Einfügen einer Eilmeldung an der aktuellen Laufstelle.
 *
 * @param titel der Titel
 * @param dauer die Dauer in Sekunden
 * @param dringlichkeitsstufe die Dringlichkeitsstufe von 1 bis 5
 * @param region die betroffene Region
 * @param laufzeit die aktuelle Laufstelle in Sekunden seit Sendungsbeginn
 */
@Schema(description = "Eilmeldung, die an der Laufstelle eingefügt wird")
public record EilmeldungEinfuegenRequest(
        @Schema(description = "Titel", example = "Sturmwarnung") String titel,
        @Schema(description = "Dauer in Sekunden", example = "45") int dauer,
        @Schema(description = "Dringlichkeitsstufe 1 bis 5", example = "4") int dringlichkeitsstufe,
        @Schema(description = "Betroffene Region", example = "Tirol") String region,
        @Schema(description = "Laufstelle in Sekunden seit Sendungsbeginn", example = "120")
                int laufzeit) {
}
