package de.bestellsystem.mahnwesen.api;

import de.bestellsystem.mahnwesen.application.Mahnlaufergebnis;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(
        name = "Mahnlauf",
        description = "Ergebnis eines Mahnlaufs über alle Kunden (US-06.2)",
        requiredProperties = {"stichtag", "gemahnteKunden", "fehlgeschlageneKunden"})
public record MahnlaufDto(
        LocalDate stichtag,
        @Schema(description = "Kunden, die eine Benachrichtigung erhalten haben")
                int gemahnteKunden,
        @Schema(description = "Kunden, deren Prüfung oder Versand gescheitert ist")
                int fehlgeschlageneKunden) {

    static MahnlaufDto von(Mahnlaufergebnis ergebnis) {
        return new MahnlaufDto(
                ergebnis.stichtag(), ergebnis.gemahnteKunden(), ergebnis.fehlgeschlageneKunden());
    }
}
