package de.bestellsystem.mahnwesen;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.bestellung.domain.Bestellwunsch;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Legt Kunden und Bestellungen mit beliebigem Bestelldatum direkt an (Zahlungsziel 14 Tage). */
public record Mahnbestand(
        KundeService kunden,
        ProduktRepository produkte,
        BestellungRepository bestellungen,
        JdbcClient jdbc) {

    private static final Anschrift HAUPTSTRASSE =
            new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");

    public UUID kunde(String vorname, String email) {
        return kunden.registriere(vorname, "Muster", email, HAUPTSTRASSE, true).getId();
    }

    public void bestellung(
            UUID kundenId,
            String nummer,
            LocalDate bestelldatum,
            long gesamtCent,
            long bezahltCent) {
        Produkt produkt =
                produkte.save(new Produkt("Artikel " + nummer, Geld.ausCent(gesamtCent), 1));
        Bestellung bestellung =
                Bestellung.gibAuf(
                        nummer,
                        kundenId,
                        bestelldatum,
                        Period.ofDays(14),
                        HAUPTSTRASSE,
                        List.of(new Bestellwunsch(produkt, 1)),
                        Zahlungsart.RECHNUNG,
                        null);
        if (bezahltCent > 0) {
            Zahlung zahlung =
                    bestellung.bucheZahlung(Zahlungsart.PAYPAL, Geld.ausCent(bezahltCent));
            bestellung.aendereZahlungsstatus(
                    zahlung.getId(), Transaktionsstatus.ERFOLGREICH, Instant.EPOCH);
        }
        bestellungen.save(bestellung);
    }

    /** Protokollierte Mahnungen als "Nummer Datum Cent", sortiert. */
    public List<String> mahnungen() {
        return jdbc.sql(
                        """
                        select bestellnummer || ' ' || erstellt_am || ' ' || gemahnter_betrag_cent
                        from mahnung order by bestellnummer, erstellt_am
                        """)
                .query(String.class)
                .list();
    }
}
