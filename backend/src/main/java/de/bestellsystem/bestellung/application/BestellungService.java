package de.bestellsystem.bestellung.application;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.bestellung.domain.Bestellwunsch;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.application.BestellnummernGenerator;
import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.gutschein.application.GutscheinService;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BestellungService {

    private final BestellungRepository repository;
    private final KundeService kundeService;
    private final ProduktService produktService;
    private final GutscheinService gutscheinService;
    private final BestellnummernGenerator bestellnummernGenerator;
    private final FachlicheEinstellungen einstellungen;
    private final Clock uhr;

    BestellungService(
            BestellungRepository repository,
            KundeService kundeService,
            ProduktService produktService,
            GutscheinService gutscheinService,
            BestellnummernGenerator bestellnummernGenerator,
            FachlicheEinstellungen einstellungen,
            Clock uhr) {
        this.repository = repository;
        this.kundeService = kundeService;
        this.produktService = produktService;
        this.gutscheinService = gutscheinService;
        this.bestellnummernGenerator = bestellnummernGenerator;
        this.einstellungen = einstellungen;
        this.uhr = uhr;
    }

    /**
     * Checkout in einer Transaktion: Bestellung, Bestandsreduktion und erste Zahlung (US-04.1).
     *
     * @param gutscheincode {@code null} oder leer ohne Gutschein
     */
    @Transactional
    public Bestellung gibAuf(
            UUID kundenId,
            List<Positionswunsch> positionen,
            UUID lieferadresseId,
            Zahlungsart zahlungsart,
            String gutscheincode) {
        Anschrift lieferanschrift = kundeService.finde(kundenId).lieferanschrift(lieferadresseId);
        Set<UUID> produktIds =
                positionen.stream().map(Positionswunsch::produktId).collect(Collectors.toSet());
        Map<UUID, Produkt> produkte = produktService.sperreZumEntnehmen(produktIds);
        List<Bestellwunsch> wuensche =
                positionen.stream()
                        .map(wunsch ->
                                new Bestellwunsch(produkte.get(wunsch.produktId()), wunsch.menge()))
                        .toList();
        Gutschein gutschein =
                gutscheincode == null || gutscheincode.isBlank()
                        ? null
                        : gutscheinService.zumEinloesen(gutscheincode);
        LocalDate heute = LocalDate.now(uhr);
        Bestellung bestellung =
                Bestellung.gibAuf(
                        bestellnummernGenerator.erzeuge(heute),
                        kundenId,
                        heute,
                        einstellungen.zahlungsziel(),
                        lieferanschrift,
                        wuensche,
                        zahlungsart,
                        gutschein);
        return repository.save(bestellung);
    }

    /** Lädt Bestellung mit Positionen, Produkten, Gutschein und Zahlungen in zwei Abfragen. */
    public Bestellung finde(UUID kundenId, String bestellnummer) {
        Bestellung bestellung =
                repository
                        .findeMitPositionen(kundenId, bestellnummer)
                        .orElseThrow(() ->
                                new NichtGefundenException(
                                        "Bestellung " + bestellnummer + " existiert nicht"));
        repository.findeZahlungen(bestellnummer);
        return bestellung;
    }
}
