package de.bestellsystem.mahnwesen.api;

import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Schema(
        name = "Mahnpruefung",
        description = "Ergebnis der Mahnprüfung eines Kunden (US-06.1)",
        requiredProperties = {"stichtag", "istGemahnt", "mahnpositionen"})
public record MahnpruefungDto(
        LocalDate stichtag,
        @Schema(description = "Eine Benachrichtigung wurde versendet") boolean istGemahnt,
        @Schema(description = "In dieser Prüfung gemahnte Bestellungen, leer ohne Rückstand")
                List<MahnpositionDto> mahnpositionen) {

    static MahnpruefungDto von(LocalDate stichtag, Optional<Mahnbenachrichtigung> mahnung) {
        return new MahnpruefungDto(
                stichtag,
                mahnung.isPresent(),
                mahnung.map(m -> m.positionen().stream().map(MahnpositionDto::von).toList())
                        .orElse(List.of()));
    }
}
