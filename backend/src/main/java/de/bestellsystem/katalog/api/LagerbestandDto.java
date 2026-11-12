package de.bestellsystem.katalog.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "Lagerbestand", description = "Neuer absoluter Lagerbestand (nie negativ, I-7)")
public record LagerbestandDto(
        @Schema(example = "5") @NotNull @PositiveOrZero Integer lagerbestand) {}
