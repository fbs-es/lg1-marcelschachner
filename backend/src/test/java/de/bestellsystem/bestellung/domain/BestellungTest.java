package de.bestellsystem.bestellung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BestellungTest {

    private static final LocalDate ZWOELFTER_NOVEMBER = LocalDate.of(2026, 11, 12);
    private static final Period ZAHLUNGSZIEL = Period.ofDays(14);
    private static final Anschrift HAUPTSTRASSE =
            new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");
    private static final UUID KUNDE = UUID.randomUUID();

    private Produkt buch;
    private Produkt lampe;

    @BeforeEach
    void katalog() {
        buch = new Produkt("Buch", Geld.ausCent(1250), 10);
        lampe = new Produkt("Lampe", Geld.ausCent(3000), 1);
    }

    private Bestellung gibAuf(List<Bestellwunsch> wuensche) {
        return gibAuf(wuensche, null, ZWOELFTER_NOVEMBER);
    }

    private static Bestellung gibAuf(
            List<Bestellwunsch> wuensche, Gutschein gutschein, LocalDate bestelldatum) {
        return Bestellung.gibAuf(
                "B-2026-000001",
                KUNDE,
                bestelldatum,
                ZAHLUNGSZIEL,
                HAUPTSTRASSE,
                wuensche,
                Zahlungsart.RECHNUNG,
                gutschein);
    }

    private List<Bestellwunsch> zweiBuecherEineLampe() {
        return List.of(new Bestellwunsch(buch, 2), new Bestellwunsch(lampe, 1));
    }

    private static Bestellung mitWarenwert(Geld preis, Gutschein gutschein, LocalDate datum) {
        Produkt produkt = new Produkt("Artikel", preis, 5);
        return gibAuf(List.of(new Bestellwunsch(produkt, 1)), gutschein, datum);
    }

    private static Bestellung mitWarenwert(Geld preis, Gutschein gutschein) {
        return mitWarenwert(preis, gutschein, ZWOELFTER_NOVEMBER);
    }

    @Nested
    class BestellungAufgeben {

        @Test
        @DisplayName("US-04.1 AK1: Bestellung mit Positionen, Gesamtwert, Fälligkeit und Zahlung")
        void wirdAngelegt() {
            Bestellung bestellung = gibAuf(zweiBuecherEineLampe());

            assertThat(bestellung.getBestellnummer()).isEqualTo("B-2026-000001");
            assertThat(bestellung.getKundeId()).isEqualTo(KUNDE);
            assertThat(bestellung.getBestelldatum()).isEqualTo(ZWOELFTER_NOVEMBER);
            assertThat(bestellung.getLieferanschrift()).isEqualTo(HAUPTSTRASSE);
            assertThat(bestellung.getPositionen())
                    .satisfiesExactly(
                            erste -> {
                                assertThat(erste.getZeilennummer()).isEqualTo(1);
                                assertThat(erste.getProdukt()).isEqualTo(buch);
                                assertThat(erste.getMenge()).isEqualTo(2);
                                assertThat(erste.getEinzelpreis()).isEqualTo(Geld.ausCent(1250));
                                assertThat(erste.berechneZwischenbetrag())
                                        .isEqualTo(Geld.ausCent(2500));
                            },
                            zweite -> {
                                assertThat(zweite.getZeilennummer()).isEqualTo(2);
                                assertThat(zweite.getProdukt()).isEqualTo(lampe);
                                assertThat(zweite.getMenge()).isEqualTo(1);
                                assertThat(zweite.getEinzelpreis()).isEqualTo(Geld.ausCent(3000));
                                assertThat(zweite.berechneZwischenbetrag())
                                        .isEqualTo(Geld.ausCent(3000));
                            });
            assertThat(bestellung.berechneWarenwert()).isEqualTo(Geld.ausCent(5500));
            assertThat(bestellung.berechneGesamtwert()).isEqualTo(Geld.ausCent(5500));
            assertThat(bestellung.getFaelligAm()).isEqualTo(LocalDate.of(2026, 11, 26));
            assertThat(bestellung.getZahlungen())
                    .singleElement()
                    .satisfies(zahlung -> {
                        assertThat(zahlung.getId()).isNotNull();
                        assertThat(zahlung.getBetrag()).isEqualTo(Geld.ausCent(5500));
                        assertThat(zahlung.getZahlungsart()).isEqualTo(Zahlungsart.RECHNUNG);
                        assertThat(zahlung.getBuchungsart()).isEqualTo(Buchungsart.ZAHLUNG);
                        assertThat(zahlung.getTransaktionsstatus())
                                .isEqualTo(Transaktionsstatus.AUSSTEHEND);
                        assertThat(zahlung.getZahlungszeitpunkt()).isEmpty();
                    });
        }

        @Test
        @DisplayName("US-04.1 AK2: Lagerbestand sinkt um die bestellte Menge")
        void bestandSinkt() {
            gibAuf(zweiBuecherEineLampe());

            assertThat(buch.getLagerbestand()).isEqualTo(8);
            assertThat(lampe.getLagerbestand()).isZero();
        }

        @Test
        @DisplayName("US-04.1 AK3: nicht genug Bestand lehnt ganze Bestellung mit Hinweis ab")
        void nichtGenugBestand() {
            List<Bestellwunsch> wuensche =
                    List.of(new Bestellwunsch(lampe, 2), new Bestellwunsch(buch, 1));

            assertThatThrownBy(() -> gibAuf(wuensche))
                    .isInstanceOf(KonfliktException.class)
                    .hasMessageContaining("Lampe")
                    .hasMessageNotContaining("Buch");
            assertThat(lampe.getLagerbestand()).isEqualTo(1);
            assertThat(buch.getLagerbestand()).isEqualTo(10);
        }

        @Test
        @DisplayName("D-17: Hinweis nennt alle Produkte, deren Bestand nicht reicht")
        void alleFehlbestaendeImHinweis() {
            List<Bestellwunsch> wuensche =
                    List.of(new Bestellwunsch(lampe, 2), new Bestellwunsch(buch, 11));

            assertThatThrownBy(() -> gibAuf(wuensche))
                    .isInstanceOf(KonfliktException.class)
                    .hasMessageContaining("Lampe")
                    .hasMessageContaining("Buch");
        }

        @Test
        @DisplayName("US-04.1 AK4: Einzelpreis ist der Listenpreis zum Checkout")
        void preisEingefroren() {
            Bestellung bestellung = gibAuf(zweiBuecherEineLampe());
            assertThat(bestellung.getPositionen().getFirst().getEinzelpreis())
                    .isEqualTo(Geld.ausCent(1250));
            assertThat(bestellung.berechneGesamtwert()).isEqualTo(Geld.ausCent(5500));
        }

        @Test
        @DisplayName("US-04.1 AK6: gleiches Produkt zweimal wird zu einer Position zusammengefasst")
        void duplikateZusammengefasst() {
            Bestellung bestellung =
                    gibAuf(List.of(new Bestellwunsch(buch, 1), new Bestellwunsch(buch, 1)));

            assertThat(bestellung.getPositionen())
                    .singleElement()
                    .satisfies(position -> {
                        assertThat(position.getZeilennummer()).isEqualTo(1);
                        assertThat(position.getProdukt()).isEqualTo(buch);
                        assertThat(position.getMenge()).isEqualTo(2);
                    });
            assertThat(buch.getLagerbestand()).isEqualTo(8);
        }

        @Test
        @DisplayName("US-04.1 AK6: Zusammenfassen prüft den Bestand gegen die Summe")
        void duplikateGegenSummeGeprueft() {
            List<Bestellwunsch> wuensche =
                    List.of(new Bestellwunsch(lampe, 1), new Bestellwunsch(lampe, 1));

            assertThatThrownBy(() -> gibAuf(wuensche)).isInstanceOf(KonfliktException.class);
            assertThat(lampe.getLagerbestand()).isEqualTo(1);
        }

        @Test
        @DisplayName("US-04.1 AK7: Bestellung ohne Positionen wird abgelehnt")
        void leereBestellung() {
            assertThatThrownBy(() -> gibAuf(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Position");
        }

        @Test
        @DisplayName("D-10: Menge muss mindestens 1 sein")
        void mengeMindestensEins() {
            assertThatThrownBy(() -> new Bestellwunsch(buch, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("I-5: Lieferanschrift und Zahlungsart sind Pflicht")
        void pflichtangaben() {
            assertThatThrownBy(() ->
                            Bestellung.gibAuf(
                                    "B-2026-000001",
                                    KUNDE,
                                    ZWOELFTER_NOVEMBER,
                                    ZAHLUNGSZIEL,
                                    null,
                                    zweiBuecherEineLampe(),
                                    Zahlungsart.RECHNUNG,
                                    null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() ->
                            Bestellung.gibAuf(
                                    "B-2026-000001",
                                    KUNDE,
                                    ZWOELFTER_NOVEMBER,
                                    ZAHLUNGSZIEL,
                                    HAUPTSTRASSE,
                                    zweiBuecherEineLampe(),
                                    null,
                                    null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class GutscheinEinloesen {

        private final Gutschein herbst10 =
                Gutschein.prozent("HERBST10", new BigDecimal("10"), LocalDate.of(2026, 11, 30));
        private final Gutschein minus15 =
                Gutschein.festbetrag("MINUS15", Geld.ausCent(1500), LocalDate.of(2026, 12, 31));
        private final Gutschein sommer20 =
                Gutschein.prozent("SOMMER20", new BigDecimal("20"), LocalDate.of(2026, 8, 31));

        @Test
        @DisplayName("US-08.1 AK1: HERBST10 auf 55,00 € ergibt Rabatt 5,50 €, Gesamtwert 49,50 €")
        void prozentrabatt() {
            Bestellung bestellung = mitWarenwert(Geld.ausCent(5500), herbst10);

            assertThat(bestellung.getRabattBetrag()).isEqualTo(Geld.ausCent(550));
            assertThat(bestellung.berechneGesamtwert()).isEqualTo(Geld.ausCent(4950));
            assertThat(bestellung.getZahlungen().getFirst().getBetrag())
                    .isEqualTo(Geld.ausCent(4950));
        }

        @Test
        @DisplayName("US-08.1 AK2: MINUS15 auf 55,00 € ergibt Gesamtwert 40,00 €")
        void festbetrag() {
            assertThat(mitWarenwert(Geld.ausCent(5500), minus15).berechneGesamtwert())
                    .isEqualTo(Geld.ausCent(4000));
        }

        @Test
        @DisplayName("US-08.1 AK3: MINUS15 auf 10,00 € ergibt Rabatt 10,00 €, Gesamtwert 0,00 €")
        void nieNegativ() {
            Bestellung bestellung = mitWarenwert(Geld.ausCent(1000), minus15);

            assertThat(bestellung.getRabattBetrag()).isEqualTo(Geld.ausCent(1000));
            assertThat(bestellung.berechneGesamtwert()).isEqualTo(Geld.NULL);
        }

        @Test
        @DisplayName("D-17: auch bei Gesamtwert 0,00 € entsteht eine ausstehende Zahlung")
        void nullEuroZahlung() {
            assertThat(mitWarenwert(Geld.ausCent(1000), minus15).getZahlungen())
                    .singleElement()
                    .satisfies(zahlung -> {
                        assertThat(zahlung.getBetrag()).isEqualTo(Geld.NULL);
                        assertThat(zahlung.getTransaktionsstatus())
                                .isEqualTo(Transaktionsstatus.AUSSTEHEND);
                    });
        }

        @Test
        @DisplayName(
                "US-08.1 AK4: abgelaufener Gutschein wird mit Hinweis abgelehnt, Bestand bleibt")
        void abgelaufen() {
            Produkt produkt = new Produkt("Artikel", Geld.ausCent(5500), 5);
            List<Bestellwunsch> wuensche = List.of(new Bestellwunsch(produkt, 1));

            assertThatThrownBy(() -> gibAuf(wuensche, sommer20, ZWOELFTER_NOVEMBER))
                    .isInstanceOf(FachlicheAblehnungException.class)
                    .hasMessageContaining("SOMMER20")
                    .hasMessageContaining("abgelaufen");
            assertThat(produkt.getLagerbestand()).isEqualTo(5);
        }

        @Test
        @DisplayName("US-08.1 AK4: nach Ablehnung kann ohne Gutschein bestellt werden")
        void ohneGutscheinWeiter() {
            Bestellung bestellung = mitWarenwert(Geld.ausCent(5500), null);

            assertThat(bestellung.getGutschein()).isEmpty();
            assertThat(bestellung.getRabattBetrag()).isEqualTo(Geld.NULL);
            assertThat(bestellung.berechneGesamtwert()).isEqualTo(Geld.ausCent(5500));
        }

        @Test
        @DisplayName("US-08.1 AK5: am Ablaufdatum 30.11.2026 wird der Rabatt gewährt")
        void ablaufdatumInklusive() {
            Bestellung bestellung =
                    mitWarenwert(Geld.ausCent(5500), herbst10, LocalDate.of(2026, 11, 30));

            assertThat(bestellung.getRabattBetrag()).isEqualTo(Geld.ausCent(550));
        }

        @Test
        @DisplayName("US-08.1 AK7: die Bestellung verweist auf höchstens einen Gutschein")
        void hoechstensEinGutschein() {
            assertThat(mitWarenwert(Geld.ausCent(5500), herbst10).getGutschein())
                    .containsSame(herbst10);
        }

        @Test
        @DisplayName("US-08.1 AK9: HERBST10 auf 0,25 € ergibt 0,03 € Rabatt")
        void kaufmaennischGerundet() {
            assertThat(mitWarenwert(Geld.ausCent(25), herbst10).getRabattBetrag())
                    .isEqualTo(Geld.ausCent(3));
        }
    }
}
