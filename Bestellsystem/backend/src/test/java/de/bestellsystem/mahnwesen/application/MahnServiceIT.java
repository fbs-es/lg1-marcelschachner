package de.bestellsystem.mahnwesen.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.BenachrichtigungsAttrappe;
import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.mahnwesen.Mahnbestand;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

/** US-06.1 mit Datenbank und Port-Attrappe; Beispieldaten der Story. */
@SpringBootTest
@Import({TestcontainersKonfiguration.class, BenachrichtigungsAttrappe.Konfiguration.class})
class MahnServiceIT {

    private static final LocalDate ERSTER_NOVEMBER = LocalDate.of(2026, 11, 1);

    @Autowired private MahnService mahnService;
    @Autowired private BenachrichtigungsAttrappe versand;
    @Autowired private JdbcClient jdbc;
    @Autowired private KundeService kundeService;
    @Autowired private ProduktRepository produkte;
    @Autowired private BestellungRepository bestellungen;

    private Mahnbestand bestand;
    private UUID anna;

    @BeforeEach
    void beispieldaten() {
        Datenbestand.leere(jdbc);
        versand.zuruecksetzen();
        bestand = new Mahnbestand(kundeService, produkte, bestellungen, jdbc);
        anna = bestand.kunde("Anna", "anna@example.de");
        bestand.bestellung(anna, "B-1", LocalDate.of(2026, 10, 1), 5_000, 0);
        bestand.bestellung(anna, "B-2", LocalDate.of(2026, 10, 5), 8_000, 3_000);
        bestand.bestellung(anna, "B-3", LocalDate.of(2026, 10, 10), 2_000, 2_000);
        bestand.bestellung(anna, "B-4", LocalDate.of(2026, 11, 10), 4_000, 0);
    }

    @AfterEach
    void aufraeumen() {
        Datenbestand.leere(jdbc);
    }

    @Test
    @DisplayName("US-06.1 AK1-3: genau eine E-Mail mit B-1 und B-2, je eine Mahnung protokolliert")
    void eineMahnungMitJedemOffenenBetrag() {
        assertThat(mahnService.pruefe(anna, ERSTER_NOVEMBER)).isPresent();

        assertThat(versand.versendet())
                .singleElement()
                .satisfies(mail -> {
                    assertThat(mail.empfaenger()).isEqualTo("anna@example.de");
                    assertThat(mail.betreff()).isEqualTo("Zahlungserinnerung");
                    assertThat(mail.text())
                            .contains("- B-1, fällig am 15.10.2026: offen 50,00 EUR")
                            .contains("- B-2, fällig am 19.10.2026: offen 50,00 EUR")
                            .contains("Summe offen: 100,00 EUR")
                            .doesNotContain("B-3")
                            .doesNotContain("B-4");
                });
        assertThat(bestand.mahnungen())
                .containsExactly("B-1 2026-11-01 5000", "B-2 2026-11-01 5000");
    }

    @Test
    @DisplayName("US-06.1 AK4: keine Nachricht am 05.11., erneute Mahnung am 08.11.")
    void keineErneuteMahnungImIntervall() {
        mahnService.pruefe(anna, ERSTER_NOVEMBER);

        assertThat(mahnService.pruefe(anna, LocalDate.of(2026, 11, 5))).isEmpty();
        assertThat(versand.versendet()).hasSize(1);

        assertThat(mahnService.pruefe(anna, LocalDate.of(2026, 11, 8))).isPresent();
        assertThat(versand.versendet()).hasSize(2);
        assertThat(bestand.mahnungen())
                .containsExactly(
                        "B-1 2026-11-01 5000",
                        "B-1 2026-11-08 5000",
                        "B-2 2026-11-01 5000",
                        "B-2 2026-11-08 5000");
    }

    @Test
    @DisplayName("US-06.1 AK5: alle überfälligen bezahlt, nichts versendet, nichts protokolliert")
    void keinRueckstandKeineNachricht() {
        UUID ben = bestand.kunde("Ben", "ben@example.de");
        bestand.bestellung(ben, "B-6", LocalDate.of(2026, 10, 1), 2_000, 2_000);

        assertThat(mahnService.pruefe(ben, ERSTER_NOVEMBER)).isEmpty();
        assertThat(versand.versendet()).isEmpty();
        assertThat(bestand.mahnungen()).isEmpty();
    }

    @Test
    @DisplayName("US-06.1 AK6: Stichtag 15.10. mahnt B-1 nicht")
    void faelligkeitstagKeinRueckstand() {
        assertThat(mahnService.pruefe(anna, LocalDate.of(2026, 10, 15))).isEmpty();
        assertThat(versand.versendet()).isEmpty();
    }

    @Test
    @DisplayName("US-06.1 AK7: Versandfehler protokolliert keine Mahnung, nächster Lauf mahnt")
    void versandfehlerProtokolliertNichts() {
        versand.scheitereFuer("anna@example.de");

        assertThatThrownBy(() -> mahnService.pruefe(anna, ERSTER_NOVEMBER))
                .isInstanceOf(BenachrichtigungNichtVersendetException.class);
        assertThat(bestand.mahnungen()).isEmpty();

        versand.zuruecksetzen();
        assertThat(mahnService.pruefe(anna, LocalDate.of(2026, 11, 2))).isPresent();
        assertThat(versand.versendet())
                .extracting(Benachrichtigung::empfaenger)
                .containsExactly("anna@example.de");
        assertThat(bestand.mahnungen())
                .containsExactly("B-1 2026-11-02 5000", "B-2 2026-11-02 5000");
    }

    @Test
    @DisplayName("Unbekannter Kunde: 404")
    void unbekannterKunde() {
        assertThatThrownBy(() -> mahnService.pruefe(UUID.randomUUID(), ERSTER_NOVEMBER))
                .isInstanceOf(NichtGefundenException.class);
    }
}
