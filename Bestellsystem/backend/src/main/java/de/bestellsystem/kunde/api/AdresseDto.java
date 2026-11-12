package de.bestellsystem.kunde.api;

import de.bestellsystem.gemeinsam.api.AnschriftDto;
import de.bestellsystem.kunde.domain.Adresse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        name = "Adresse",
        requiredProperties = {"id", "anschrift", "istLieferadresse", "istRechnungsadresse"})
public record AdresseDto(
        UUID id, AnschriftDto anschrift, boolean istLieferadresse, boolean istRechnungsadresse) {

    static AdresseDto von(Adresse adresse) {
        return new AdresseDto(
                adresse.getId(),
                AnschriftDto.von(adresse.getAnschrift()),
                adresse.istLieferadresse(),
                adresse.istRechnungsadresse());
    }
}
