package de.bestellsystem.zahlung.api;

import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "StatusAenderung",
        description = "Neuer Status einer Buchung; nur aus AUSSTEHEND möglich (D-19)",
        requiredProperties = {"transaktionsstatus"})
public record StatusAenderungDto(
        @Schema(example = "ERFOLGREICH") @NotNull Transaktionsstatus transaktionsstatus) {}
