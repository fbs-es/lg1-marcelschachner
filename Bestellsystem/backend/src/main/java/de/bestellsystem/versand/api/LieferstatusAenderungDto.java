package de.bestellsystem.versand.api;

import de.bestellsystem.versand.domain.Lieferstatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "LieferstatusAenderung",
        description = "Neuer Lieferstatus eines Pakets; nur erlaubte Folgestatus (D-22)",
        requiredProperties = {"lieferstatus"})
public record LieferstatusAenderungDto(
        @Schema(example = "UEBERGEBEN") @NotNull Lieferstatus lieferstatus) {}
