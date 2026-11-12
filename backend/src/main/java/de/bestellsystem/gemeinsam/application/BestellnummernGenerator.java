package de.bestellsystem.gemeinsam.application;

import java.time.LocalDate;
import org.springframework.stereotype.Service;

/** Erzeugt fachliche Bestellnummern im Format {@code B-JJJJ-NNNNNN} (D-13, D-14). */
@Service
public class BestellnummernGenerator {

    private final BestellnummernZaehler zaehler;

    public BestellnummernGenerator(BestellnummernZaehler zaehler) {
        this.zaehler = zaehler;
    }

    public String erzeuge(LocalDate bestelldatum) {
        return "B-%d-%06d".formatted(bestelldatum.getYear(), zaehler.naechsterWert());
    }
}
