package de.bestellsystem.kunde.api;

import de.bestellsystem.kunde.domain.Kunde;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        name = "KundeUebersicht",
        description = "Kunde ohne Adressbuch, für Listen",
        requiredProperties = {"id", "vorname", "nachname", "email"})
public record KundeUebersichtDto(UUID id, String vorname, String nachname, String email) {

    static KundeUebersichtDto von(Kunde kunde) {
        return new KundeUebersichtDto(
                kunde.getId(), kunde.getVorname(), kunde.getNachname(), kunde.getEmail().wert());
    }
}
