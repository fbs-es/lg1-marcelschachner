package de.bestellsystem.katalog.application;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
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

    public boolean istVerfuegbar(UUID produktId, int menge) {
        return finde(produktId).istVerfuegbar(menge);
    }
}
