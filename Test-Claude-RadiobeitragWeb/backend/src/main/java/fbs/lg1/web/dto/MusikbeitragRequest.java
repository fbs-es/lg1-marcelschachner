package fbs.lg1.web.dto;

import fbs.lg1.Musikbeitrag;
import fbs.lg1.Musikgenre;
import fbs.lg1.Radiobeitrag;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage für einen neuen Musikbeitrag ({@code "typ": "MUSIK"}).
 *
 * @param titel der Titel
 * @param dauer die Dauer in Sekunden
 * @param kuenstler der Künstler
 * @param album das Album
 * @param genre das Musikgenre
 */
@Schema(description = "Neuer Musikbeitrag")
public record MusikbeitragRequest(
        @Schema(description = "Titel", example = "Wolkenlos") String titel,
        @Schema(description = "Dauer in Sekunden", example = "240") int dauer,
        @Schema(description = "Künstler", example = "Die Wolken") String kuenstler,
        @Schema(description = "Album", example = "Himmelblau") String album,
        @Schema(description = "Musikgenre", example = "POP") Musikgenre genre)
        implements BeitragRequest {

    /**
     * Erstellt einen {@link Musikbeitrag} aus den Anfragedaten.
     */
    @Override
    public Radiobeitrag erstelleBeitrag() {
        return new Musikbeitrag(titel, dauer, kuenstler, album, genre);
    }
}
