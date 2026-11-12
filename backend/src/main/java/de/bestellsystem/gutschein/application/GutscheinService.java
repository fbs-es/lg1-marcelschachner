package de.bestellsystem.gutschein.application;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.domain.RabattArt;
import de.bestellsystem.gutschein.persistence.GutscheinRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GutscheinService {

    private final GutscheinRepository repository;
    private final Clock uhr;

    GutscheinService(GutscheinRepository repository, Clock uhr) {
        this.repository = repository;
        this.uhr = uhr;
    }

    /** US-08.2, D-23: 409 bei vergebenem Code, auch bei gleichzeitigem Anlegen. */
    @Transactional
    public Gutschein legeAn(
            String code,
            RabattArt rabattArt,
            BigDecimal prozent,
            Geld festbetrag,
            LocalDate ablaufdatum) {
        Gutschein gutschein =
                Gutschein.legeAn(code, rabattArt, prozent, festbetrag, ablaufdatum, heute());
        if (repository.existsByCode(gutschein.getCode())) {
            throw codeVergeben(gutschein.getCode());
        }
        try {
            return repository.saveAndFlush(gutschein);
        } catch (DataIntegrityViolationException gleichzeitigAngelegt) {
            throw codeVergeben(gutschein.getCode());
        }
    }

    public List<Gutschein> alle() {
        return repository.findAllByOrderByCodeAsc();
    }

    public Gutschein finde(UUID gutscheinId) {
        return repository
                .findById(gutscheinId)
                .orElseThrow(() ->
                        new NichtGefundenException(
                                "Gutschein " + gutscheinId + " existiert nicht"));
    }

    public LocalDate heute() {
        return LocalDate.now(uhr);
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

    private static KonfliktException codeVergeben(String code) {
        return new KonfliktException("Der Gutscheincode " + code + " ist bereits vergeben.");
    }
}
