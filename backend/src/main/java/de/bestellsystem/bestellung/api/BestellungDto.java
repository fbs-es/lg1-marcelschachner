package de.bestellsystem.bestellung.api;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.gemeinsam.api.AnschriftDto;
import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.versand.api.VersandpaketDto;
import de.bestellsystem.zahlung.api.ZahlungDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(
        name = "Bestellung",
        requiredProperties = {
            "bestellnummer",
            "kundenId",
            "bestelldatum",
            "faelligAm",
            "lieferanschrift",
            "positionen",
            "warenwert",
            "rabattBetrag",
            "gesamtwert",
            "zahlungen",
            "bezahlteSumme",
            "offenerBetrag",
            "istVollstaendigBezahlt",
            "pakete"
        })
public record BestellungDto(
        @Schema(example = "B-2026-000123") String bestellnummer,
        UUID kundenId,
        LocalDate bestelldatum,
        @Schema(description = "bestelldatum + Zahlungsziel (D-03)") LocalDate faelligAm,
        @Schema(description = "Snapshot zum Checkout (D-08)") AnschriftDto lieferanschrift,
        List<BestellpositionDto> positionen,
        @Schema(description = "Summe der Zwischenbeträge") GeldDto warenwert,
        @Schema(description = "Eingelöster Gutschein, leer ohne Gutschein") String gutscheincode,
        @Schema(description = "Beim Checkout eingefroren (D-04)") GeldDto rabattBetrag,
        @Schema(description = "max(0, warenwert − rabattBetrag), berechnet (D-01)")
                GeldDto gesamtwert,
        List<ZahlungDto> zahlungen,
        @Schema(description = "Σ erfolgreiche Zahlungen − Σ erfolgreiche Erstattungen (D-05)")
                GeldDto bezahlteSumme,
        @Schema(description = "max(0, gesamtwert − bezahlteSumme) (D-05)") GeldDto offenerBetrag,
        @Schema(description = "bezahlteSumme ≥ gesamtwert (D-05)") boolean istVollstaendigBezahlt,
        @Schema(
                        description =
                                "Versandpakete in Anlagereihenfolge; leer, solange nichts versandt ist")
                List<VersandpaketDto> pakete) {

    static BestellungDto von(Bestellung bestellung) {
        return new BestellungDto(
                bestellung.getBestellnummer(),
                bestellung.getKundeId(),
                bestellung.getBestelldatum(),
                bestellung.getFaelligAm(),
                AnschriftDto.von(bestellung.getLieferanschrift()),
                bestellung.getPositionen().stream().map(BestellpositionDto::von).toList(),
                GeldDto.von(bestellung.berechneWarenwert()),
                bestellung.getGutschein().map(Gutschein::getCode).orElse(null),
                GeldDto.von(bestellung.getRabattBetrag()),
                GeldDto.von(bestellung.berechneGesamtwert()),
                bestellung.getZahlungen().stream().map(ZahlungDto::von).toList(),
                GeldDto.von(bestellung.berechneBezahlteSumme()),
                GeldDto.von(bestellung.berechneOffenenBetrag()),
                bestellung.istVollstaendigBezahlt(),
                bestellung.getPakete().stream().map(VersandpaketDto::von).toList());
    }
}
