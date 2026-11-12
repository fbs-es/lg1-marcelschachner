package de.bestellsystem.gutschein.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.domain.RabattArt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(
        name = "Gutschein",
        requiredProperties = {"id", "code", "rabattArt", "ablaufdatum", "istGueltig"})
public record GutscheinDto(
        UUID id,
        String code,
        RabattArt rabattArt,
        @Schema(description = "Nur bei PROZENT") BigDecimal prozent,
        @Schema(description = "Nur bei FESTBETRAG") GeldDto festbetrag,
        LocalDate ablaufdatum,
        @Schema(description = "Heute laut Uhr nicht nach dem Ablaufdatum (D-04)")
                boolean istGueltig) {

    static GutscheinDto von(Gutschein gutschein, LocalDate heute) {
        return new GutscheinDto(
                gutschein.getId(),
                gutschein.getCode(),
                gutschein.getRabattArt(),
                gutschein.getProzent(),
                gutschein.getFestbetrag() == null ? null : GeldDto.von(gutschein.getFestbetrag()),
                gutschein.getAblaufdatum(),
                gutschein.istGueltigAm(heute));
    }
}
