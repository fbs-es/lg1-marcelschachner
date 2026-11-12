package de.bestellsystem.merkliste.application;

import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.merkliste.domain.Merklisteneintrag;
import de.bestellsystem.merkliste.domain.MerklisteneintragId;
import de.bestellsystem.merkliste.persistence.MerklisteneintragRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Merkliste eines Kunden (E03, D-07, D-21). */
@Service
@Transactional(readOnly = true)
public class MerklisteService {

    private final MerklisteneintragRepository repository;
    private final KundeService kundeService;
    private final ProduktService produktService;
    private final Clock uhr;

    MerklisteService(
            MerklisteneintragRepository repository,
            KundeService kundeService,
            ProduktService produktService,
            Clock uhr) {
        this.repository = repository;
        this.kundeService = kundeService;
        this.produktService = produktService;
        this.uhr = uhr;
    }

    /** Merkt das Produkt jetzt; war es schon gemerkt, bleibt der Eintrag unverändert (I-4). */
    @Transactional
    public Merkergebnis merke(UUID kundenId, UUID produktId) {
        kundeService.pruefeVorhanden(kundenId);
        produktService.finde(produktId);
        boolean istNeu = repository.merke(kundenId, produktId, uhr.instant()) == 1;
        return new Merkergebnis(findeEintrag(kundenId, produktId), istNeu);
    }

    /** Neueste zuerst, mit aktuellem Produkt (D-07, D-21). */
    public List<Merklisteneintrag> merkliste(UUID kundenId) {
        kundeService.pruefeVorhanden(kundenId);
        return repository.findeMerkliste(kundenId);
    }

    public Merklisteneintrag eintrag(UUID kundenId, UUID produktId) {
        kundeService.pruefeVorhanden(kundenId);
        return findeEintrag(kundenId, produktId);
    }

    private Merklisteneintrag findeEintrag(UUID kundenId, UUID produktId) {
        return repository
                .findeEintrag(new MerklisteneintragId(kundenId, produktId))
                .orElseThrow(() ->
                        new NichtGefundenException(
                                "Produkt " + produktId + " steht nicht auf der Merkliste"));
    }

    /** Entfernt den Eintrag; war das Produkt nicht gemerkt, ändert sich nichts (D-21). */
    @Transactional
    public void entferne(UUID kundenId, UUID produktId) {
        kundeService.pruefeVorhanden(kundenId);
        repository.entferne(new MerklisteneintragId(kundenId, produktId));
    }

    public record Merkergebnis(Merklisteneintrag eintrag, boolean istNeu) {}
}
