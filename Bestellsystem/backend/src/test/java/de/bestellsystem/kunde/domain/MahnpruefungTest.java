package de.bestellsystem.kunde.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.bestellung.domain.Bestellwunsch;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import de.bestellsystem.mahnwesen.domain.Mahnposition;
import de.bestellsystem.mahnwesen.domain.Mahnung;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** US-06.1 mit den Beispieldaten der Story; Zahlungsziel 14 Tage, Mahnintervall 7 Tage. */
class MahnpruefungTest {

    private static final Period ZAHLUNGSZIEL = Period.ofDays(14);
    private static final Period MAHNINTERVALL = Period.ofDays(7);
    private static final Anschrift HAUPTSTRASSE =
            new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");

    private Kunde anna;
    private Bestellung b1;
    private Bestellung b2;
    private Bestellung b3;
    private Bestellung b4;
    private List<Bestellung> bestellungen;

    @BeforeEach
    void beispieldaten() {
        anna = new Kunde("Anna", "Muster", new Email("anna@example.de"), HAUPTSTRASSE, true);
        b1 = bestellung(anna, "B-1", LocalDate.of(2026, 10, 1), 5_000, 0);
        b2 = bestellung(anna, "B-2", LocalDate.of(2026, 10, 5), 8_000, 3_000);
        b3 = bestellung(anna, "B-3", LocalDate.of(2026, 10, 10), 2_000, 2_000);
        b4 = bestellung(anna, "B-4", LocalDate.of(2026, 11, 10), 4_000, 0);
        bestellungen = List.of(b1, b2, b3, b4);
    }

    static Bestellung bestellung(
            Kunde kunde, String nummer, LocalDate bestelldatum, long gesamtCent, long bezahltCent) {
        Bestellung bestellung =
                Bestellung.gibAuf(
                        nummer,
                        kunde.getId(),
                        bestelldatum,
                        ZAHLUNGSZIEL,
                        HAUPTSTRASSE,
                        List.of(
                                new Bestellwunsch(
                                        new Produkt("Artikel", Geld.ausCent(gesamtCent), 1), 1)),
                        Zahlungsart.RECHNUNG,
                        null);
        if (bezahltCent > 0) {
            Zahlung zahlung =
                    bestellung.bucheZahlung(Zahlungsart.PAYPAL, Geld.ausCent(bezahltCent));
            bestellung.aendereZahlungsstatus(
                    zahlung.getId(), Transaktionsstatus.ERFOLGREICH, Instant.EPOCH);
        }
        return bestellung;
    }

    private Optional<Mahnbenachrichtigung> pruefe(LocalDate stichtag) {
        return anna.fuehreMahnpruefungDurch(stichtag, bestellungen, MAHNINTERVALL);
    }

    @Test
    @DisplayName("US-06.1 AK1: eine Benachrichtigung listet B-1 und B-2 mit je 50,00 € offen")
    void eineMahnungMitJedemOffenenBetrag() {
        Mahnbenachrichtigung mahnung = pruefe(LocalDate.of(2026, 11, 1)).orElseThrow();

        assertThat(mahnung.empfaenger()).isEqualTo("anna@example.de");
        assertThat(mahnung.stichtag()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(mahnung.positionen())
                .containsExactly(
                        new Mahnposition("B-1", LocalDate.of(2026, 10, 15), Geld.ausCent(5_000)),
                        new Mahnposition("B-2", LocalDate.of(2026, 10, 19), Geld.ausCent(5_000)));
        assertThat(mahnung.summe()).isEqualTo(Geld.ausCent(10_000));
        assertThat(b1.getMahnungen())
                .singleElement()
                .satisfies(protokoll -> {
                    assertThat(protokoll.getErstelltAm()).isEqualTo(LocalDate.of(2026, 11, 1));
                    assertThat(protokoll.getGemahnterBetrag()).isEqualTo(Geld.ausCent(5_000));
                });
        assertThat(b2.getMahnungen())
                .extracting(Mahnung::getErstelltAm)
                .containsExactly(LocalDate.of(2026, 11, 1));
    }

    @Test
    @DisplayName("US-06.1 AK2: bezahlte Bestellung B-3 wird nicht gemahnt")
    void bezahlteNichtGemahnt() {
        Mahnbenachrichtigung mahnung = pruefe(LocalDate.of(2026, 11, 1)).orElseThrow();

        assertThat(mahnung.positionen())
                .extracting(Mahnposition::bestellnummer)
                .doesNotContain("B-3");
        assertThat(b3.getMahnungen()).isEmpty();
    }

    @Test
    @DisplayName("US-06.1 AK3: noch nicht fällige B-4 wird nicht gemahnt, obwohl unbezahlt")
    void nichtFaelligeNichtGemahnt() {
        Mahnbenachrichtigung mahnung = pruefe(LocalDate.of(2026, 11, 1)).orElseThrow();

        assertThat(mahnung.positionen())
                .extracting(Mahnposition::bestellnummer)
                .doesNotContain("B-4");
        assertThat(b4.getMahnungen()).isEmpty();
    }

    @Test
    @DisplayName("US-06.1 AK4: keine erneute Mahnung am 05.11., erneut am 08.11.")
    void keineErneuteMahnungImIntervall() {
        pruefe(LocalDate.of(2026, 11, 1)).orElseThrow();

        assertThat(pruefe(LocalDate.of(2026, 11, 5))).isEmpty();
        assertThat(b1.getMahnungen()).hasSize(1);

        Mahnbenachrichtigung erneut = pruefe(LocalDate.of(2026, 11, 8)).orElseThrow();
        assertThat(erneut.positionen())
                .extracting(Mahnposition::bestellnummer)
                .containsExactly("B-1", "B-2");
        assertThat(b1.getMahnungen())
                .extracting(Mahnung::getErstelltAm)
                .containsExactly(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 8));
    }

    @Test
    @DisplayName("US-06.1 AK5: alle überfälligen bezahlt, keine Nachricht, nichts protokolliert")
    void keinRueckstandKeineNachricht() {
        bestellungen = List.of(b3);

        assertThat(pruefe(LocalDate.of(2026, 11, 1))).isEmpty();
        assertThat(b3.getMahnungen()).isEmpty();
    }

    @Test
    @DisplayName("US-06.1 AK6: am Fälligkeitstag 15.10. wird B-1 nicht gemahnt")
    void faelligkeitstagKeinRueckstand() {
        assertThat(pruefe(LocalDate.of(2026, 10, 15))).isEmpty();
        assertThat(b1.istUeberfaellig(LocalDate.of(2026, 10, 15))).isFalse();
        assertThat(b1.istUeberfaellig(LocalDate.of(2026, 10, 16))).isTrue();
    }

    @Test
    @DisplayName("D-20: E-Mail listet nur Bestellungen, deren Mahnung fällig ist")
    void nurFaelligeBestellungenInDerNachricht() {
        bestellungen = List.of(b1);
        pruefe(LocalDate.of(2026, 11, 1)).orElseThrow();
        Bestellung b5 = bestellung(anna, "B-5", LocalDate.of(2026, 10, 20), 1_000, 0);
        bestellungen = List.of(b1, b5);

        Mahnbenachrichtigung mahnung = pruefe(LocalDate.of(2026, 11, 4)).orElseThrow();

        assertThat(mahnung.positionen())
                .extracting(Mahnposition::bestellnummer)
                .containsExactly("B-5");
        assertThat(b1.getMahnungen()).hasSize(1);
    }

    @Test
    @DisplayName("D-20: Mahnung friert den offenen Betrag am Stichtag ein")
    void gemahnterBetragEingefroren() {
        pruefe(LocalDate.of(2026, 11, 1)).orElseThrow();
        Zahlung teilzahlung = b1.bucheZahlung(Zahlungsart.PAYPAL, Geld.ausCent(2_000));
        b1.aendereZahlungsstatus(
                teilzahlung.getId(), Transaktionsstatus.ERFOLGREICH, Instant.EPOCH);

        assertThat(b1.getMahnungen().getFirst().getGemahnterBetrag())
                .isEqualTo(Geld.ausCent(5_000));
        assertThat(pruefe(LocalDate.of(2026, 11, 8)).orElseThrow().positionen().getFirst())
                .isEqualTo(
                        new Mahnposition("B-1", LocalDate.of(2026, 10, 15), Geld.ausCent(3_000)));
    }

    @Test
    @DisplayName("D-20: Mahnintervall unter einem Tag wird abgelehnt")
    void intervallMindestensEinTag() {
        assertThatThrownBy(() ->
                        anna.fuehreMahnpruefungDurch(
                                LocalDate.of(2026, 11, 1), bestellungen, Period.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Bestellung eines anderen Kunden wird abgelehnt")
    void fremdeBestellungAbgelehnt() {
        Kunde ben = new Kunde("Ben", "Beispiel", new Email("ben@example.de"), HAUPTSTRASSE, true);
        bestellungen = List.of(bestellung(ben, "B-9", LocalDate.of(2026, 10, 1), 1_000, 0));

        assertThatThrownBy(() -> pruefe(LocalDate.of(2026, 11, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
