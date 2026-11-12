package de.bestellsystem.gemeinsam.api;

import de.bestellsystem.gemeinsam.domain.Geld;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "Geld", description = "Geldbetrag in ganzen Cent (D-11)")
public record GeldDto(
        @Schema(description = "Betrag in Cent", example = "4999") @NotNull @PositiveOrZero
                Long cent,
        @Schema(description = "Währung, immer EUR", example = "EUR", allowableValues = "EUR")
                @NotNull
                @Pattern(regexp = Geld.WAEHRUNG, message = "muss EUR sein")
                String waehrung) {

    public static GeldDto von(Geld geld) {
        return new GeldDto(geld.cent(), geld.waehrung());
    }

    public Geld alsGeld() {
        return Geld.ausCent(cent);
    }
}
