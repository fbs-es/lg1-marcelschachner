package de.bestellsystem.kunde.api;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "Anschrift",
        requiredProperties = {"strasse", "stadt", "postleitzahl", "land"})
public record AnschriftDto(
        @Schema(example = "Hauptstraße 1") @NotBlank @Size(max = Anschrift.STRASSE_MAXIMALE_LAENGE)
                String strasse,
        @Schema(example = "Berlin") @NotBlank @Size(max = Anschrift.STADT_MAXIMALE_LAENGE)
                String stadt,
        @Schema(example = "10115") @NotBlank @Size(max = Anschrift.POSTLEITZAHL_MAXIMALE_LAENGE)
                String postleitzahl,
        @Schema(description = "ISO-3166-Alpha-2-Code", example = "DE") @NotBlank String land) {

    static AnschriftDto von(Anschrift anschrift) {
        return new AnschriftDto(
                anschrift.strasse(), anschrift.stadt(), anschrift.postleitzahl(), anschrift.land());
    }

    Anschrift alsAnschrift() {
        return new Anschrift(strasse, stadt, postleitzahl, land);
    }
}
