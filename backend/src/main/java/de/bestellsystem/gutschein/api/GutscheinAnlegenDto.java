package de.bestellsystem.gutschein.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.domain.RabattArt;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        name = "GutscheinAnlegen",
        description =
                "Neuer Gutschein (D-04, D-23). Genau einer von prozent und festbetrag, passend zur"
                        + " Rabattart.")
public record GutscheinAnlegenDto(
        @Schema(
                        description =
                                "1–50 Buchstaben, Ziffern und -; wird getrimmt und großgeschrieben",
                        example = "HERBST10",
                        pattern = Gutschein.CODE_MUSTER)
                @NotBlank
                String code,
        @NotNull RabattArt rabattArt,
        @Schema(
                        description =
                                "Nur bei PROZENT: größer 0, höchstens 100, zwei Nachkommastellen",
                        example = "10")
                BigDecimal prozent,
        @Schema(description = "Nur bei FESTBETRAG: größer 0") @Valid GeldDto festbetrag,
        @Schema(description = "Letzter gültiger Tag, nicht vor heute", example = "2026-12-31")
                @NotNull
                LocalDate ablaufdatum) {}
