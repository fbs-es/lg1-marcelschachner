package de.bestellsystem.gutschein.application;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.persistence.GutscheinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GutscheinService {

    private final GutscheinRepository repository;

    GutscheinService(GutscheinRepository repository) {
        this.repository = repository;
    }

    /** Unbekannte Codes sind eine fachliche Ablehnung des Checkouts (US-08.1 AK6, D-17). */
    public Gutschein zumEinloesen(String code) {
        String normalisiert = Gutschein.normalisiere(code);
        return repository
                .findByCode(normalisiert)
                .orElseThrow(() ->
                        new FachlicheAblehnungException(
                                "Der Gutschein " + normalisiert + " ist unbekannt."));
    }
}
