package de.bestellsystem.katalog.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.katalog.domain.Produkt;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(name = "ProduktAnlegen", description = "Neues Produkt (D-15)")
public record ProduktAnlegenDto(
        @Schema(example = "Stehlampe") @NotBlank @Size(max = Produkt.TITEL_MAXIMALE_LAENGE)
                String titel,
        @NotNull @Valid GeldDto listenpreis,
        @Schema(description = "Anfangsbestand", example = "3") @NotNull @PositiveOrZero
                Integer lagerbestand) {}
