package de.bestellsystem.bestellung.api;

import de.bestellsystem.bestellung.application.Positionswunsch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(
        name = "Positionswunsch",
        description = "Gleiches Produkt mehrfach wird zusammengefasst (D-10)",
        requiredProperties = {"produktId", "menge"})
public record PositionswunschDto(
        @NotNull UUID produktId, @Schema(example = "2") @NotNull @Min(1) Integer menge) {

    Positionswunsch alsPositionswunsch() {
        return new Positionswunsch(produktId, menge);
    }
}
