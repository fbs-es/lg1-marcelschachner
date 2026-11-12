package de.bestellsystem.mahnwesen.application;

import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.gemeinsam.application.Benachrichtigungsversand;
import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mahnprüfung für ein Kundenkonto (US-06.1, D-03, D-20). */
@Service
public class MahnService {

    private final KundeService kundeService;
    private final BestellungService bestellungService;
    private final Benachrichtigungsversand versand;
    private final FachlicheEinstellungen einstellungen;

    MahnService(
            KundeService kundeService,
            BestellungService bestellungService,
            Benachrichtigungsversand versand,
            FachlicheEinstellungen einstellungen) {
        this.kundeService = kundeService;
        this.bestellungService = bestellungService;
        this.versand = versand;
        this.einstellungen = einstellungen;
    }

    /**
     * Mahnt fällige Bestellungen und versendet eine Nachricht. Der Versand läuft vor dem Commit:
     * scheitert er, rollt die Transaktion zurück und keine Mahnung bleibt protokolliert (AK7).
     *
     * @throws de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException wenn
     *     der Versand scheitert
     */
    @Transactional
    public Optional<Mahnbenachrichtigung> pruefe(UUID kundenId, LocalDate stichtag) {
        Kunde kunde = kundeService.sperreZumMahnen(kundenId);
        Optional<Mahnbenachrichtigung> mahnung =
                kunde.fuehreMahnpruefungDurch(
                        stichtag,
                        bestellungService.faelligeZumMahnen(kundenId, stichtag),
                        einstellungen.mahnintervall());
        mahnung.map(Mahnschreiben::von).ifPresent(versand::sende);
        return mahnung;
    }
}
