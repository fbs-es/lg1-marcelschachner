package de.bestellsystem.katalog.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.katalog.domain.Produkt;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ProduktAendern", description = "Neuer Titel und Listenpreis (D-15, D-23)")
public record ProduktAendernDto(
        @Schema(example = "Stehlampe Nova") @NotBlank @Size(max = Produkt.TITEL_MAXIMALE_LAENGE)
                String titel,
        @NotNull @Valid GeldDto listenpreis) {}
