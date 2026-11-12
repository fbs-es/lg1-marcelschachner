package de.bestellsystem.versand.api;

import de.bestellsystem.versand.domain.Logistikdienstleister;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "PaketAnlegen",
        description = "Neues Versandpaket; entsteht mit Lieferstatus ANGELEGT (US-07.1)",
        requiredProperties = {"dienstleister", "sendungsnummer"})
public record PaketAnlegenDto(
        @Schema(example = "DHL") @NotNull Logistikdienstleister dienstleister,
        @Schema(
                        description =
                                "1–50 Buchstaben oder Ziffern; wird getrimmt und großgeschrieben"
                                        + " (D-22)",
                        example = "00340434161094042557")
                @NotBlank
                String sendungsnummer) {}
