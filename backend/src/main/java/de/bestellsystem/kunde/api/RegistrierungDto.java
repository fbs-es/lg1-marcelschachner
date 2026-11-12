package de.bestellsystem.kunde.api;

import de.bestellsystem.gemeinsam.api.AnschriftDto;
import de.bestellsystem.kunde.domain.Kunde;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        name = "Registrierung",
        description = "Neues Kundenkonto mit erster Lieferadresse (US-01.1)")
public record RegistrierungDto(
        @Schema(example = "Anna") @NotBlank @Size(max = Kunde.NAME_MAXIMALE_LAENGE) String vorname,
        @Schema(example = "Muster") @NotBlank @Size(max = Kunde.NAME_MAXIMALE_LAENGE)
                String nachname,
        @Schema(
                        description = "Eindeutig ohne Groß-/Kleinschreibung (D-13)",
                        example = "anna@example.de")
                @NotBlank
                String email,
        @NotNull(message = "Eine Lieferadresse ist erforderlich") @Valid AnschriftDto lieferadresse,
        @Schema(description = "Lieferadresse ist zugleich Rechnungsadresse", defaultValue = "false")
                boolean istAuchRechnungsadresse) {}
