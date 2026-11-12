package de.bestellsystem.bestellung.api;

import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(
        name = "BestellungAufgeben",
        description = "Checkout (US-04.1, US-08.1)",
        requiredProperties = {"positionen", "lieferadresseId", "zahlungsart"})
public record BestellungAufgebenDto(
        @NotEmpty(message = "Eine Bestellung braucht mindestens eine Position") @Valid
                List<@NotNull PositionswunschDto> positionen,
        @Schema(description = "Adresse aus dem eigenen Adressbuch mit Lieferkennzeichen") @NotNull
                UUID lieferadresseId,
        @NotNull Zahlungsart zahlungsart,
        @Schema(
                        description =
                                "Höchstens ein Code, ohne Beachtung der Groß-/Kleinschreibung"
                                        + " (D-04)",
                        example = "HERBST10")
                @Size(max = Gutschein.CODE_MAXIMALE_LAENGE)
                String gutscheincode) {}
