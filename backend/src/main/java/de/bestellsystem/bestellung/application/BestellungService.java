package de.bestellsystem.bestellung.application;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.bestellung.domain.Bestellwunsch;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.application.BestellnummernGenerator;
import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.gutschein.application.GutscheinService;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.versand.domain.Lieferstatus;
import de.bestellsystem.versand.domain.Logistikdienstleister;
import de.bestellsystem.versand.domain.Versandpaket;
import de.bestellsystem.versand.persistence.VersandpaketRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BestellungService {

    private final BestellungRepository repository;
    private final VersandpaketRepository versandpakete;
    private final KundeService kundeService;
    private final ProduktService produktService;
    private final GutscheinService gutscheinService;
    private final BestellnummernGenerator bestellnummernGenerator;
    private final FachlicheEinstellungen einstellungen;
    private final Clock uhr;
    private final EntityManager entityManager;

    BestellungService(
            BestellungRepository repository,
            VersandpaketRepository versandpakete,
            KundeService kundeService,
            ProduktService produktService,
            GutscheinService gutscheinService,
            BestellnummernGenerator bestellnummernGenerator,
            FachlicheEinstellungen einstellungen,
            Clock uhr,
            EntityManager entityManager) {
        this.repository = repository;
        this.versandpakete = versandpakete;
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

    /**
     * Lädt Bestellung mit Positionen, Produkten, Gutschein, Zahlungen und Paketen in drei Abfragen.
     */
    public Bestellung finde(UUID kundenId, String bestellnummer) {
        Bestellung bestellung =
                repository
                        .findeMitPositionen(kundenId, bestellnummer)
                        .orElseThrow(() -> nichtGefunden(bestellnummer));
        repository.findeZahlungen(bestellnummer);
        repository.findePakete(bestellnummer);
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

    /**
     * Bestellungen des Kunden, deren Fälligkeit vor dem Stichtag liegt, mit Positionen, Zahlungen
     * und Mahnungen in drei Abfragen (E06).
     */
    public List<Bestellung> faelligeZumMahnen(UUID kundenId, LocalDate stichtag) {
        List<Bestellung> bestellungen = repository.findeFaelligeMitPositionen(kundenId, stichtag);
        if (!bestellungen.isEmpty()) {
            repository.findeZahlungenFaelliger(kundenId, stichtag);
            repository.findeMahnungenFaelliger(kundenId, stichtag);
        }
        return bestellungen;
    }

    /** Kunden mit mindestens einer Bestellung, deren Fälligkeit vor dem Stichtag liegt (D-20). */
    public List<UUID> kundenMitFaelligenBestellungen(LocalDate stichtag) {
        return repository.findeKundenMitFaelligenBestellungen(stichtag);
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

    /** Ein Paket der Bestellung des Kunden; 404 bei fremder Bestellung oder fremdem Paket. */
    public Versandpaket paket(UUID kundenId, String bestellnummer, UUID paketId) {
        return repository
                .findeMitPaketen(kundenId, bestellnummer)
                .orElseThrow(() -> nichtGefunden(bestellnummer))
                .paket(paketId);
    }

    /**
     * Neues Paket mit Status ANGELEGT (US-07.1); 409, wenn die Sendungsnummer beim Dienstleister
     * schon vergeben ist (I-11).
     */
    @Transactional
    public Versandpaket legePaketAn(
            UUID kundenId,
            String bestellnummer,
            Logistikdienstleister dienstleister,
            String sendungsnummer) {
        Bestellung bestellung =
                gesperrt(
                        repository
                                .findeMitPaketen(kundenId, bestellnummer)
                                .orElseThrow(() -> nichtGefunden(bestellnummer)));
        String normalisiert = Versandpaket.normalisiere(sendungsnummer);
        if (versandpakete.existsByDienstleisterAndSendungsnummer(dienstleister, normalisiert)) {
            throw sendungsnummerVergeben(dienstleister, normalisiert);
        }
        Versandpaket paket = bestellung.legePaketAn(dienstleister, normalisiert, Instant.now(uhr));
        try {
            repository.flush();
        } catch (DataIntegrityViolationException gleichzeitigAngelegt) {
            throw sendungsnummerVergeben(dienstleister, normalisiert);
        }
        return paket;
    }

    /** Lieferstatus fortschreiben (US-07.2, D-22); Zeitpunkt aus der Uhr. */
    @Transactional
    public Versandpaket aendereLieferstatus(
            UUID kundenId, String bestellnummer, UUID paketId, Lieferstatus status) {
        return gesperrt(
                        repository
                                .findeMitPaketen(kundenId, bestellnummer)
                                .orElseThrow(() -> nichtGefunden(bestellnummer)))
                .aendereLieferstatus(paketId, status, Instant.now(uhr));
    }

    private Bestellung zumBuchen(UUID kundenId, String bestellnummer) {
        return gesperrt(
                repository
                        .findeMitZahlungen(kundenId, bestellnummer)
                        .orElseThrow(() -> nichtGefunden(bestellnummer)));
    }

    // Erhöht die Version beim Festschreiben, auch wenn sich nur eine Zahlung oder ein Paket ändert;
    // so scheitert eine gleichzeitige zweite Änderung derselben Bestellung (D-19).
    private Bestellung gesperrt(Bestellung bestellung) {
        entityManager.lock(bestellung, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return bestellung;
    }

    private static KonfliktException sendungsnummerVergeben(
            Logistikdienstleister dienstleister, String sendungsnummer) {
        return new KonfliktException(
                "Sendungsnummer %s ist bei %s schon vergeben."
                        .formatted(sendungsnummer, dienstleister));
    }

    private static NichtGefundenException nichtGefunden(String bestellnummer) {
        return new NichtGefundenException("Bestellung " + bestellnummer + " existiert nicht");
    }
}
