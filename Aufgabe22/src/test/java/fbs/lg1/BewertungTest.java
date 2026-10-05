package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

class BewertungTest {

    private Kunde kunde;
    private Produkt produkt;
    private Kauf kauf;
    private Bewertung bewertung;

    @BeforeEach
    void prepare() {
        Hersteller hersteller = new Hersteller("Logitech", "support@logitech.com");
        produkt = new Produkt("P-001", "Maus", new BigDecimal("19.99"), hersteller);
        kunde = new Kunde("K-001", "Marcel Schachner");
        kauf = new Kauf(kunde, produkt, LocalDate.now());
        bewertung = new Bewertung(kunde, produkt, 4, "Gute Maus", kauf);
    }

    @Test
    void testInit() {
        assertThat(bewertung.showKunde()).isSameAs(kunde);
        assertThat(bewertung.showProdukt()).isSameAs(produkt);
        assertThat(bewertung.showSterne()).isEqualTo(4);
        assertThat(bewertung.showKommentar()).isEqualTo("Gute Maus");
        assertThat(bewertung.showErstellungsDatum()).isEqualTo(LocalDate.now());
        assertThat(bewertung.showKauf()).isSameAs(kauf);
        assertThat(bewertung.istGekauft()).isTrue();
        assertThat(bewertung.showAntwort()).isNull();
        assertThat(produkt.showBewertungen()).containsExactly(bewertung);
    }

    @Test
    void testNichtGekauft() {
        Bewertung ohneKauf = new Bewertung(kunde, produkt, 2, "Nur getestet", null);

        assertThat(ohneKauf.showKauf()).isNull();
        assertThat(ohneKauf.istGekauft()).isFalse();
    }

    @Test
    void testKaufFremderKunde() {
        Kunde max = new Kunde("K-002", "Max Mustermann");

        assertThatThrownBy(() -> new Bewertung(max, produkt, 3, "Gut", kauf))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.showBewertungen()).containsExactly(bewertung);
    }

    @Test
    void testKaufFremdesProdukt() {
        Produkt tastatur =
                new Produkt("P-002", "Tastatur", new BigDecimal("49.99"), produkt.showHersteller());

        assertThatThrownBy(() -> new Bewertung(kunde, tastatur, 3, "Gut", kauf))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(tastatur.showBewertungen()).isEmpty();
    }

    @Test
    void testKaufBereitsBewertet() {
        assertThatThrownBy(() -> new Bewertung(kunde, produkt, 1, "Nochmal", kauf))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.showBewertungen()).containsExactly(bewertung);
    }

    @Test
    void testKundeBewertetVieleProdukte() {
        Produkt tastatur =
                new Produkt("P-002", "Tastatur", new BigDecimal("49.99"), produkt.showHersteller());

        Bewertung zweite = new Bewertung(kunde, tastatur, 5, "Top", null);

        assertThat(zweite.showKunde()).isSameAs(bewertung.showKunde());
        assertThat(tastatur.showBewertungen()).containsExactly(zweite);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Bewertung(null, produkt, 4, "Gut", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bewertung(kunde, null, 4, "Gut", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.showBewertungen()).containsExactly(bewertung);
    }

    @Test
    void testOhneKommentar() {
        Bewertung ohneKommentar = new Bewertung(kunde, produkt, 3, null, null);
        assertThat(ohneKommentar.showKommentar()).isNull();
        assertThat(produkt.showBewertungen()).contains(ohneKommentar);
    }

    @Test
    void testSterneGrenzen() {
        assertThatThrownBy(() -> new Bewertung(kunde, produkt, 0, "Gut", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bewertung(kunde, produkt, 6, "Gut", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new Bewertung(kunde, produkt, 1, "Schlecht", null).showSterne()).isEqualTo(1);
        assertThat(new Bewertung(kunde, produkt, 5, "Top", null).showSterne()).isEqualTo(5);
    }

    @Test
    void testAntworten() {
        Antwort antwort = bewertung.antworten("Danke!");

        assertThat(bewertung.showAntwort()).isSameAs(antwort);
        assertThat(antwort.showBewertung()).isSameAs(bewertung);
    }

    @Test
    void testAntwortenZweimal() {
        Antwort erste = bewertung.antworten("Danke!");

        assertThatThrownBy(() -> bewertung.antworten("Nochmal danke!"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bewertung.showAntwort()).isSameAs(erste);
    }

    @Test
    void testAntwortenLeererText() {
        assertThatThrownBy(() -> bewertung.antworten(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bewertung.antworten(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(bewertung.showAntwort()).isNull();
    }
}
