package de.bestellsystem.zahlung.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "ErstattungBuchen",
        description = "Erstattung, entsteht mit Status AUSSTEHEND (D-19)",
        requiredProperties = {"zahlungsart", "betrag"})
public record ErstattungBuchenDto(
        @NotNull Zahlungsart zahlungsart,
        @Schema(description = "Größer 0, höchstens der erstattbare Betrag") @NotNull @Valid
                GeldDto betrag) {}
