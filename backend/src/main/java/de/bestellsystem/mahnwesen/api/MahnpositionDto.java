package de.bestellsystem.mahnwesen.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.mahnwesen.domain.Mahnposition;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(
        name = "Mahnposition",
        description = "Gemahnte Bestellung mit offenem Betrag am Stichtag (D-20)",
        requiredProperties = {"bestellnummer", "faelligAm", "offenerBetrag"})
public record MahnpositionDto(
        @Schema(example = "B-2026-000123") String bestellnummer,
        LocalDate faelligAm,
        GeldDto offenerBetrag) {

    static MahnpositionDto von(Mahnposition position) {
        return new MahnpositionDto(
                position.bestellnummer(),
                position.faelligAm(),
                GeldDto.von(position.offenerBetrag()));
    }
}
