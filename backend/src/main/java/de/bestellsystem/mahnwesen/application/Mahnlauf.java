package de.bestellsystem.mahnwesen.application;

import de.bestellsystem.bestellung.application.BestellungService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Täglicher Mahnlauf über alle Kunden mit fälligen Bestellungen (US-06.2, D-20). */
@Component
public class Mahnlauf {

    private static final Logger LOG = LoggerFactory.getLogger(Mahnlauf.class);

    private final BestellungService bestellungService;
    private final MahnService mahnService;
    private final Clock uhr;

    Mahnlauf(BestellungService bestellungService, MahnService mahnService, Clock uhr) {
        this.bestellungService = bestellungService;
        this.mahnService = mahnService;
        this.uhr = uhr;
    }

    @Scheduled(cron = "${bestellsystem.mahnlauf.cron}", zone = "${bestellsystem.zeitzone}")
    public Mahnlaufergebnis fuehreHeuteDurch() {
        return fuehreDurch(LocalDate.now(uhr));
    }

    /**
     * Prüft jeden Kandidaten in eigener Transaktion; ein Fehler bei einem Kunden hält den Lauf
     * nicht auf (D-20).
     */
    public Mahnlaufergebnis fuehreDurch(LocalDate stichtag) {
        int gemahnt = 0;
        int fehlgeschlagen = 0;
        for (UUID kundenId : bestellungService.kundenMitFaelligenBestellungen(stichtag)) {
            try {
                if (mahnService.pruefe(kundenId, stichtag).isPresent()) {
                    gemahnt++;
                }
            } catch (RuntimeException fehler) {
                fehlgeschlagen++;
                LOG.warn("Mahnprüfung für Kunde {} am {} gescheitert", kundenId, stichtag, fehler);
            }
        }
        Mahnlaufergebnis ergebnis = new Mahnlaufergebnis(stichtag, gemahnt, fehlgeschlagen);
        LOG.info("Mahnlauf beendet: {}", ergebnis);
        return ergebnis;
    }
}
