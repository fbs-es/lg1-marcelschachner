package de.bestellsystem.kunde.application;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.kunde.domain.Adresse;
import de.bestellsystem.kunde.domain.Email;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.kunde.persistence.KundeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KundeService {

    private final KundeRepository repository;
    private final EntityManager entityManager;

    KundeService(KundeRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Kunde registriere(
            String vorname,
            String nachname,
            String email,
            Anschrift lieferadresse,
            boolean istAuchRechnungsadresse) {
        Kunde kunde =
                new Kunde(
                        vorname,
                        nachname,
                        new Email(email),
                        lieferadresse,
                        istAuchRechnungsadresse);
        if (repository.existsByEmail(kunde.getEmail())) {
            throw emailVergeben(kunde.getEmail());
        }
        try {
            return repository.saveAndFlush(kunde);
        } catch (DataIntegrityViolationException gleichzeitigRegistriert) {
            throw emailVergeben(kunde.getEmail());
        }
    }

    public List<Kunde> alle() {
        return repository.findAllByOrderByNachnameAscVornameAsc();
    }

    public Kunde finde(UUID kundenId) {
        return repository.findeMitAdressen(kundenId).orElseThrow(() -> nichtGefunden(kundenId));
    }

    /** Lädt und sperrt den Kunden bis zum Ende der Transaktion (D-20); 404, wenn unbekannt. */
    public Kunde sperreZumMahnen(UUID kundenId) {
        return repository.sperreZumMahnen(kundenId).orElseThrow(() -> nichtGefunden(kundenId));
    }

    /** Wirft 404, wenn der Kunde nicht existiert, ohne ihn zu laden. */
    public void pruefeVorhanden(UUID kundenId) {
        if (!repository.existsById(kundenId)) {
            throw nichtGefunden(kundenId);
        }
    }

    public Adresse findeAdresse(UUID kundenId, UUID adressId) {
        return finde(kundenId).adresse(adressId);
    }

    @Transactional
    public Adresse fuegeAdresseHinzu(
            UUID kundenId,
            Anschrift anschrift,
            boolean istLieferadresse,
            boolean istRechnungsadresse) {
        return zumAendern(kundenId)
                .fuegeAdresseHinzu(anschrift, istLieferadresse, istRechnungsadresse);
    }

    @Transactional
    public Adresse aendereAdresse(
            UUID kundenId,
            UUID adressId,
            Anschrift anschrift,
            boolean istLieferadresse,
            boolean istRechnungsadresse) {
        return zumAendern(kundenId)
                .aendereAdresse(adressId, anschrift, istLieferadresse, istRechnungsadresse);
    }

    @Transactional
    public void entferneAdresse(UUID kundenId, UUID adressId) {
        zumAendern(kundenId).entferneAdresse(adressId);
    }

    // Erhöht die Version des Kunden beim Festschreiben, auch wenn sich nur eine Adresse ändert;
    // so scheitert eine gleichzeitige zweite Änderung am Adressbuch (D-16).
    private Kunde zumAendern(UUID kundenId) {
        Kunde kunde = finde(kundenId);
        entityManager.lock(kunde, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return kunde;
    }

    private static KonfliktException emailVergeben(Email email) {
        return new KonfliktException(
                "Die E-Mail-Adresse " + email.wert() + " ist bereits vergeben.");
    }

    private static NichtGefundenException nichtGefunden(UUID kundenId) {
        return new NichtGefundenException("Kunde " + kundenId + " existiert nicht");
    }
}
