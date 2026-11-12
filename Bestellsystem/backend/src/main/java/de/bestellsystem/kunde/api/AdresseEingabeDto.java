package de.bestellsystem.kunde.api;

import de.bestellsystem.gemeinsam.api.AnschriftDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "AdresseEingabe",
        description = "Adresse mit Kennzeichen; mindestens eines muss gesetzt sein (I-2)")
public record AdresseEingabeDto(
        @NotNull @Valid AnschriftDto anschrift,
        @NotNull Boolean istLieferadresse,
        @NotNull Boolean istRechnungsadresse) {}
