package de.bestellsystem.bestellung.application;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.bestellung.domain.Bestellwunsch;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.application.BestellnummernGenerator;
import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.gutschein.application.GutscheinService;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Clock;
import java.time.Instant;
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
    private final EntityManager entityManager;

    BestellungService(
            BestellungRepository repository,
            KundeService kundeService,
            ProduktService produktService,
            GutscheinService gutscheinService,
            BestellnummernGenerator bestellnummernGenerator,
            FachlicheEinstellungen einstellungen,
            Clock uhr,
            EntityManager entityManager) {
        this.repository = repository;
        this.kundeService = kundeService;
        this.produktService = produktService;
        this.gutscheinService = gutscheinService;
        this.bestellnummernGenerator = bestellnummernGenerator;
        this.einstellungen = einstellungen;
        this.uhr = uhr;
        this.entityManager = entityManager;
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
                        .orElseThrow(() -> nichtGefunden(bestellnummer));
        repository.findeZahlungen(bestellnummer);
        return bestellung;
    }

    /**
     * Bestellungen des Kunden mit Positionen und Zahlungen, neueste zuerst (US-04.2, D-19); 404 bei
     * unbekanntem Kunden.
     */
    public List<Bestellung> historie(UUID kundenId) {
        kundeService.pruefeVorhanden(kundenId);
        List<Bestellung> bestellungen = repository.findeHistorie(kundenId);
        if (!bestellungen.isEmpty()) {
            repository.findeZahlungenDesKunden(kundenId);
        }
        return bestellungen;
    }

    /** Eine Buchung der Bestellung des Kunden; 404 bei fremder Bestellung oder Buchung. */
    public Zahlung zahlung(UUID kundenId, String bestellnummer, UUID zahlungId) {
        return repository
                .findeMitZahlungen(kundenId, bestellnummer)
                .orElseThrow(() -> nichtGefunden(bestellnummer))
                .zahlung(zahlungId);
    }

    /** Statuswechsel einer Buchung (D-19); Zeitpunkt aus der Uhr. */
    @Transactional
    public Zahlung aendereZahlungsstatus(
            UUID kundenId, String bestellnummer, UUID zahlungId, Transaktionsstatus status) {
        return zumBuchen(kundenId, bestellnummer)
                .aendereZahlungsstatus(zahlungId, status, Instant.now(uhr));
    }

    /** Erstattung mit Status AUSSTEHEND (D-19). */
    @Transactional
    public Zahlung bucheErstattung(
            UUID kundenId, String bestellnummer, Zahlungsart zahlungsart, Geld betrag) {
        return zumBuchen(kundenId, bestellnummer).bucheErstattung(zahlungsart, betrag);
    }

    // Erhöht die Version beim Festschreiben, auch wenn sich nur der Status einer Zahlung ändert;
    // so scheitert eine gleichzeitige zweite Buchung auf derselben Bestellung (D-19).
    private Bestellung zumBuchen(UUID kundenId, String bestellnummer) {
        Bestellung bestellung =
                repository
                        .findeMitZahlungen(kundenId, bestellnummer)
                        .orElseThrow(() -> nichtGefunden(bestellnummer));
        entityManager.lock(bestellung, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return bestellung;
    }

    private static NichtGefundenException nichtGefunden(String bestellnummer) {
        return new NichtGefundenException("Bestellung " + bestellnummer + " existiert nicht");
    }
}
