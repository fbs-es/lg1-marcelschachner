package de.bestellsystem.bestellung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** US-05.1 und D-19; jede Bestellung hat einen Gesamtwert von 100,00 €. */
class BezahlstatusTest {

    private static final LocalDate ZWOELFTER_NOVEMBER = LocalDate.of(2026, 11, 12);
    private static final Instant JETZT = Instant.parse("2026-11-12T10:00:00Z");
    private static final Geld HUNDERT = Geld.ausCent(10_000);

    private Bestellung bestellung;
    private Zahlung ersteZahlung;

    @BeforeEach
    void bestellungUeber100Euro() {
        bestellung = bestellung(HUNDERT, null);
        ersteZahlung = bestellung.getZahlungen().getFirst();
    }

    private static Bestellung bestellung(Geld warenwert, Gutschein gutschein) {
        return Bestellung.gibAuf(
                "B-2026-000001",
                UUID.randomUUID(),
                ZWOELFTER_NOVEMBER,
                Period.ofDays(14),
                new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                List.of(new Bestellwunsch(new Produkt("Artikel", warenwert, 5), 1)),
                Zahlungsart.RECHNUNG,
                gutschein);
    }

    private void setze(Zahlung zahlung, Transaktionsstatus status) {
        bestellung.aendereZahlungsstatus(zahlung.getId(), status, JETZT);
    }

    private Zahlung erfolgreicheZahlung(long cent) {
        Zahlung zahlung = bestellung.bucheZahlung(Zahlungsart.PAYPAL, Geld.ausCent(cent));
        setze(zahlung, Transaktionsstatus.ERFOLGREICH);
        return zahlung;
    }

    @Nested
    class VollstaendigBezahlt {

        @Test
        @DisplayName("US-05.1 AK1: nur ausstehende Zahlung, offen 100,00 €")
        void nurAusstehend() {
            assertThat(bestellung.istVollstaendigBezahlt()).isFalse();
            assertThat(bestellung.berechneBezahlteSumme()).isEqualTo(Geld.NULL);
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(HUNDERT);
        }

        @Test
        @DisplayName("US-05.1 AK2: Zahlung 100,00 € erfolgreich, vollständig bezahlt, offen 0,00 €")
        void vollstaendig() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            assertThat(bestellung.istVollstaendigBezahlt()).isTrue();
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(Geld.NULL);
        }

        @Test
        @DisplayName("US-05.1 AK3: Teilzahlungen 40,00 € und 60,00 € ergeben vollständig bezahlt")
        void teilzahlungen() {
            setze(ersteZahlung, Transaktionsstatus.STORNIERT);
            erfolgreicheZahlung(4_000);
            erfolgreicheZahlung(6_000);

            assertThat(bestellung.istVollstaendigBezahlt()).isTrue();
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(Geld.NULL);
        }

        @Test
        @DisplayName("US-05.1 AK3: nur 40,00 € erfolgreich, offen 60,00 €")
        void teilweise() {
            setze(ersteZahlung, Transaktionsstatus.STORNIERT);
            erfolgreicheZahlung(4_000);
            bestellung.bucheZahlung(Zahlungsart.PAYPAL, Geld.ausCent(6_000));

            assertThat(bestellung.istVollstaendigBezahlt()).isFalse();
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(Geld.ausCent(6_000));
        }

        @Test
        @DisplayName("US-05.1 AK4: fehlgeschlagene und stornierte Zahlungen zählen nicht")
        void fehlgeschlagenUndStorniert() {
            setze(ersteZahlung, Transaktionsstatus.FEHLGESCHLAGEN);
            setze(
                    bestellung.bucheZahlung(Zahlungsart.PAYPAL, HUNDERT),
                    Transaktionsstatus.STORNIERT);

            assertThat(bestellung.istVollstaendigBezahlt()).isFalse();
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(HUNDERT);
        }

        @Test
        @DisplayName("US-05.1 AK5: Erstattung 30,00 € reduziert bezahlte Summe auf 70,00 €")
        void erstattung() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);
            setze(
                    bestellung.bucheErstattung(Zahlungsart.RECHNUNG, Geld.ausCent(3_000)),
                    Transaktionsstatus.ERFOLGREICH);

            assertThat(bestellung.berechneBezahlteSumme()).isEqualTo(Geld.ausCent(7_000));
            assertThat(bestellung.istVollstaendigBezahlt()).isFalse();
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(Geld.ausCent(3_000));
        }

        @Test
        @DisplayName("US-05.1 AK6: Überzahlung 120,00 € ergibt offen 0,00 €, nicht negativ")
        void ueberzahlung() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);
            erfolgreicheZahlung(2_000);

            assertThat(bestellung.berechneBezahlteSumme()).isEqualTo(Geld.ausCent(12_000));
            assertThat(bestellung.istVollstaendigBezahlt()).isTrue();
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(Geld.NULL);
        }

        @Test
        @DisplayName("US-05.1 AK7: Warenwert 100,00 € mit Rabatt 10,00 €, Zahlung 90,00 € genügt")
        void mitRabatt() {
            bestellung =
                    bestellung(
                            HUNDERT,
                            Gutschein.festbetrag(
                                    "MINUS10", Geld.ausCent(1_000), LocalDate.of(2026, 12, 31)));
            setze(bestellung.getZahlungen().getFirst(), Transaktionsstatus.ERFOLGREICH);

            assertThat(bestellung.getZahlungen().getFirst().getBetrag())
                    .isEqualTo(Geld.ausCent(9_000));
            assertThat(bestellung.istVollstaendigBezahlt()).isTrue();
        }
    }

    @Nested
    class StatusAendern {

        @ParameterizedTest
        @EnumSource(
                value = Transaktionsstatus.class,
                names = {"ERFOLGREICH", "FEHLGESCHLAGEN", "STORNIERT"})
        @DisplayName("D-19: aus AUSSTEHEND in jeden Endzustand")
        void ausAusstehend(Transaktionsstatus ziel) {
            setze(ersteZahlung, ziel);

            assertThat(ersteZahlung.getTransaktionsstatus()).isEqualTo(ziel);
        }

        @Test
        @DisplayName("D-19: ERFOLGREICH setzt den Zahlungszeitpunkt")
        void zeitpunktBeiErfolg() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            assertThat(ersteZahlung.getZahlungszeitpunkt()).contains(JETZT);
        }

        @ParameterizedTest
        @EnumSource(
                value = Transaktionsstatus.class,
                names = {"FEHLGESCHLAGEN", "STORNIERT"})
        @DisplayName("D-19: ohne Erfolg bleibt der Zahlungszeitpunkt leer")
        void keinZeitpunktOhneErfolg(Transaktionsstatus ziel) {
            setze(ersteZahlung, ziel);

            assertThat(ersteZahlung.getZahlungszeitpunkt()).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(Transaktionsstatus.class)
        @DisplayName("D-19: Endzustände sind endgültig (422)")
        void endzustandEndgueltig(Transaktionsstatus ziel) {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            assertThatThrownBy(() -> setze(ersteZahlung, ziel))
                    .isInstanceOf(FachlicheAblehnungException.class)
                    .hasMessageContaining("ERFOLGREICH");
            assertThat(ersteZahlung.getTransaktionsstatus())
                    .isEqualTo(Transaktionsstatus.ERFOLGREICH);
        }

        @Test
        @DisplayName("D-19: AUSSTEHEND nach AUSSTEHEND wird abgelehnt (422)")
        void ausstehendNachAusstehend() {
            assertThatThrownBy(() -> setze(ersteZahlung, Transaktionsstatus.AUSSTEHEND))
                    .isInstanceOf(FachlicheAblehnungException.class);
        }

        @Test
        @DisplayName("D-19: unbekannte Zahlung ergibt 404")
        void unbekannteZahlung() {
            assertThatThrownBy(() ->
                            bestellung.aendereZahlungsstatus(
                                    UUID.randomUUID(), Transaktionsstatus.ERFOLGREICH, JETZT))
                    .isInstanceOf(NichtGefundenException.class);
        }

        @Test
        @DisplayName("D-19: Status ist Pflicht")
        void ohneStatus() {
            assertThatThrownBy(() -> setze(ersteZahlung, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class ErstattungBuchen {

        @Test
        @DisplayName("D-19: Erstattung entsteht AUSSTEHEND mit Zahlungsart und Betrag")
        void entstehtAusstehend() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            Zahlung erstattung =
                    bestellung.bucheErstattung(Zahlungsart.LASTSCHRIFT, Geld.ausCent(3_000));

            assertThat(erstattung.getBuchungsart()).isEqualTo(Buchungsart.ERSTATTUNG);
            assertThat(erstattung.getTransaktionsstatus()).isEqualTo(Transaktionsstatus.AUSSTEHEND);
            assertThat(erstattung.getZahlungsart()).isEqualTo(Zahlungsart.LASTSCHRIFT);
            assertThat(erstattung.getBetrag()).isEqualTo(Geld.ausCent(3_000));
            assertThat(erstattung.getZahlungszeitpunkt()).isEmpty();
            assertThat(bestellung.getZahlungen()).contains(erstattung);
            assertThat(bestellung.berechneBezahlteSumme()).isEqualTo(HUNDERT);
        }

        @Test
        @DisplayName("D-19: die gesamte bezahlte Summe ist erstattbar")
        void allesErstattbar() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            setze(
                    bestellung.bucheErstattung(Zahlungsart.RECHNUNG, HUNDERT),
                    Transaktionsstatus.ERFOLGREICH);

            assertThat(bestellung.berechneBezahlteSumme()).isEqualTo(Geld.NULL);
            assertThat(bestellung.berechneOffenenBetrag()).isEqualTo(HUNDERT);
        }

        @Test
        @DisplayName("D-19: mehr als die bezahlte Summe wird abgelehnt (422)")
        void mehrAlsBezahlt() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            assertThatThrownBy(() ->
                            bestellung.bucheErstattung(Zahlungsart.RECHNUNG, Geld.ausCent(10_001)))
                    .isInstanceOf(FachlicheAblehnungException.class)
                    .hasMessageContaining("100,00 EUR");
            assertThat(bestellung.getZahlungen()).hasSize(1);
        }

        @Test
        @DisplayName("D-19: ohne erfolgreiche Zahlung ist nichts erstattbar (422)")
        void nichtsBezahlt() {
            assertThatThrownBy(
                            () -> bestellung.bucheErstattung(Zahlungsart.RECHNUNG, Geld.ausCent(1)))
                    .isInstanceOf(FachlicheAblehnungException.class);
        }

        @Test
        @DisplayName("D-19: ausstehende Erstattungen verringern den erstattbaren Betrag")
        void ausstehendeErstattungZaehlt() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);
            bestellung.bucheErstattung(Zahlungsart.RECHNUNG, Geld.ausCent(7_000));

            assertThat(bestellung.berechneErstattbarenBetrag()).isEqualTo(Geld.ausCent(3_000));
            assertThatThrownBy(() ->
                            bestellung.bucheErstattung(Zahlungsart.RECHNUNG, Geld.ausCent(3_001)))
                    .isInstanceOf(FachlicheAblehnungException.class);
        }

        @Test
        @DisplayName("D-19: fehlgeschlagene Erstattung gibt den Betrag wieder frei")
        void fehlgeschlageneErstattung() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);
            setze(
                    bestellung.bucheErstattung(Zahlungsart.RECHNUNG, HUNDERT),
                    Transaktionsstatus.FEHLGESCHLAGEN);

            assertThat(bestellung.berechneErstattbarenBetrag()).isEqualTo(HUNDERT);
            assertThat(bestellung.istVollstaendigBezahlt()).isTrue();
        }

        @Test
        @DisplayName("D-19: Betrag 0,00 € ist ungültig (400)")
        void nullBetrag() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            assertThatThrownBy(() -> bestellung.bucheErstattung(Zahlungsart.RECHNUNG, Geld.NULL))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("D-19: Zahlungsart ist Pflicht (400)")
        void ohneZahlungsart() {
            setze(ersteZahlung, Transaktionsstatus.ERFOLGREICH);

            assertThatThrownBy(() -> bestellung.bucheErstattung(null, Geld.ausCent(100)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("D-19: Teilzahlung entsteht als ZAHLUNG mit Status AUSSTEHEND, Betrag > 0")
    void teilzahlung() {
        Zahlung zahlung = bestellung.bucheZahlung(Zahlungsart.PAYPAL, Geld.ausCent(4_000));

        assertThat(zahlung.getBuchungsart()).isEqualTo(Buchungsart.ZAHLUNG);
        assertThat(zahlung.getTransaktionsstatus()).isEqualTo(Transaktionsstatus.AUSSTEHEND);
        assertThat(bestellung.getZahlungen()).hasSize(2);
        assertThatThrownBy(() -> bestellung.bucheZahlung(Zahlungsart.PAYPAL, Geld.NULL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
