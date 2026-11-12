package de.bestellsystem.kunde.api;

import de.bestellsystem.kunde.domain.Kunde;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(
        name = "Kunde",
        description = "Kunde mit Adressbuch in Anlagereihenfolge",
        requiredProperties = {"id", "vorname", "nachname", "email", "adressen"})
public record KundeDto(
        UUID id, String vorname, String nachname, String email, List<AdresseDto> adressen) {

    static KundeDto von(Kunde kunde) {
        return new KundeDto(
                kunde.getId(),
                kunde.getVorname(),
                kunde.getNachname(),
                kunde.getEmail().wert(),
                kunde.getAdressen().stream().map(AdresseDto::von).toList());
    }
}
