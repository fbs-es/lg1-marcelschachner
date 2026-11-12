package de.bestellsystem.mahnwesen.application;

import static org.assertj.core.api.Assertions.assertThat;

import de.bestellsystem.BenachrichtigungsAttrappe;
import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.mahnwesen.Mahnbestand;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.config.ScheduledTaskHolder;

/** US-06.2 mit den Beispieldaten der Story. */
@SpringBootTest
@Import({TestcontainersKonfiguration.class, BenachrichtigungsAttrappe.Konfiguration.class})
class MahnlaufIT {

    private static final LocalDate ERSTER_NOVEMBER = LocalDate.of(2026, 11, 1);

    @Autowired private Mahnlauf mahnlauf;
    @Autowired private BenachrichtigungsAttrappe versand;
    @Autowired private JdbcClient jdbc;
    @Autowired private KundeService kundeService;
    @Autowired private ProduktRepository produkte;
    @Autowired private BestellungRepository bestellungen;
    @Autowired private ScheduledTaskHolder zeitplan;

    private Mahnbestand bestand;

    @BeforeEach
    void beispieldaten() {
        Datenbestand.leere(jdbc);
        versand.zuruecksetzen();
        bestand = new Mahnbestand(kundeService, produkte, bestellungen, jdbc);
        bestand.bestellung(
                bestand.kunde("Anna", "anna@example.de"),
                "B-1",
                LocalDate.of(2026, 10, 1),
                5_000,
                0);
        bestand.bestellung(
                bestand.kunde("Ben", "ben@example.de"), "B-6", LocalDate.of(2026, 10, 6), 3_000, 0);
        bestand.bestellung(
                bestand.kunde("Clara", "clara@example.de"),
                "B-7",
                LocalDate.of(2026, 10, 6),
                3_000,
                3_000);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("US-06.2 AK1: Anna und Ben je eine Nachricht, Clara keine")
    void alleKundenMitRueckstand() {
        Mahnlaufergebnis ergebnis = mahnlauf.fuehreDurch(ERSTER_NOVEMBER);

        assertThat(ergebnis).isEqualTo(new Mahnlaufergebnis(ERSTER_NOVEMBER, 2, 0));
        assertThat(versand.versendet())
                .extracting(Benachrichtigung::empfaenger)
                .containsExactlyInAnyOrder("anna@example.de", "ben@example.de");
        assertThat(bestand.mahnungen())
                .containsExactly("B-1 2026-11-01 5000", "B-6 2026-11-01 3000");
    }

    @Test
    @DisplayName("US-06.2 AK2: Versandfehler bei Anna hält den Lauf nicht auf")
    void fehlerBeiEinemKunden() {
        versand.scheitereFuer("anna@example.de");

        Mahnlaufergebnis ergebnis = mahnlauf.fuehreDurch(ERSTER_NOVEMBER);

        assertThat(ergebnis).isEqualTo(new Mahnlaufergebnis(ERSTER_NOVEMBER, 1, 1));
        assertThat(versand.versendet())
                .extracting(Benachrichtigung::empfaenger)
                .containsExactly("ben@example.de");
        assertThat(bestand.mahnungen()).containsExactly("B-6 2026-11-01 3000");
    }

    @Test
    @DisplayName("US-06.2 AK3: Wiederholung am selben Tag versendet und protokolliert nichts")
    void idempotentAmSelbenTag() {
        mahnlauf.fuehreDurch(ERSTER_NOVEMBER);

        Mahnlaufergebnis wiederholung = mahnlauf.fuehreDurch(ERSTER_NOVEMBER);

        assertThat(wiederholung).isEqualTo(new Mahnlaufergebnis(ERSTER_NOVEMBER, 0, 0));
        assertThat(versand.versendet()).hasSize(2);
        assertThat(bestand.mahnungen()).hasSize(2);
    }

    @Test
    @DisplayName("US-06.2 AK4: mit cron \"-\" ist kein zeitgesteuerter Lauf eingeplant")
    void abschaltbar() {
        assertThat(zeitplan.getScheduledTasks()).isEmpty();
    }
}
