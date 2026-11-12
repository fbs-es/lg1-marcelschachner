package de.bestellsystem.katalog.application;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProduktService {

    private final ProduktRepository repository;

    ProduktService(ProduktRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Produkt legeAn(String titel, Geld listenpreis, int lagerbestand) {
        return repository.save(new Produkt(titel, listenpreis, lagerbestand));
    }

    public List<Produkt> alle() {
        return repository.findAllByOrderByTitelAsc();
    }

    public Produkt finde(UUID produktId) {
        return repository
                .findById(produktId)
                .orElseThrow(() ->
                        new NichtGefundenException("Produkt " + produktId + " existiert nicht"));
    }

    @Transactional
    public Produkt setzeLagerbestand(UUID produktId, int lagerbestand) {
        Produkt produkt = finde(produktId);
        produkt.setzeLagerbestand(lagerbestand);
        return produkt;
    }

    /**
     * Sperrt die Produkte bis zum Ende der laufenden Transaktion, damit gleichzeitige Checkouts den
     * Bestand nacheinander prüfen (ADR 0003).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Map<UUID, Produkt> sperreZumEntnehmen(Set<UUID> produktIds) {
        if (produktIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Produkt> produkte =
                repository.sperreZumEntnehmen(produktIds).stream()
                        .collect(Collectors.toMap(Produkt::getId, Function.identity()));
        List<UUID> unbekannt =
                produktIds.stream().filter(id -> !produkte.containsKey(id)).sorted().toList();
        if (!unbekannt.isEmpty()) {
            throw new NichtGefundenException("Produkte existieren nicht: " + unbekannt);
        }
        return produkte;
    }

    public boolean istVerfuegbar(UUID produktId, int menge) {
        return finde(produktId).istVerfuegbar(menge);
    }
}
