package fbs.lg1;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ProduktTest {

    private Hersteller hersteller;
    private Produkt produkt;
    private Kunde kunde;

    @BeforeEach
    void prepare() {
        hersteller = new Hersteller("Logitech", "support@logitech.com");
        produkt = new Produkt("P-001", "MX Master 3S", new BigDecimal("99.99"), hersteller);
        kunde = new Kunde("K-001", "Marcel Schachner");
    }

    @Test
    void testInit() {
        assertThat(produkt.showProduktNummer()).isEqualTo("P-001");
        assertThat(produkt.showName()).isEqualTo("MX Master 3S");
        assertThat(produkt.showPreis()).isEqualByComparingTo("99.99");
        assertThat(produkt.showHersteller()).isSameAs(hersteller);
        assertThat(produkt.showBewertungen()).isEmpty();
    }

    @Test
    void testInitInvalid() {
        BigDecimal preis = new BigDecimal("10");
        assertThatThrownBy(() -> new Produkt(null, "Maus", preis, hersteller))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt(" ", "Maus", preis, hersteller))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt("P-1", null, preis, hersteller))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt("P-1", " ", preis, hersteller))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt("P-1", "Maus", null, hersteller))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt("P-1", "Maus", BigDecimal.ZERO, hersteller))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt("P-1", "Maus", new BigDecimal("-1"), hersteller))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Produkt("P-1", "Maus", preis, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testHerstellerMitVielenProdukten() {
        Produkt tastatur = new Produkt("P-002", "MX Keys", new BigDecimal("119.00"), hersteller);

        assertThat(tastatur.showHersteller()).isSameAs(produkt.showHersteller());
    }

    @Test
    void testDurchschnittOhneBewertungen() {
        assertThat(produkt.berechneDurchschnittssterne()).isEqualTo(0.0);
    }

    @Test
    void testDurchschnittEineBewertung() {
        new Bewertung(kunde, produkt, 4, "Gut", true);

        assertThat(produkt.berechneDurchschnittssterne()).isEqualTo(4.0);
    }

    @Test
    void testDurchschnittMehrereBewertungen() {
        new Bewertung(kunde, produkt, 5, "Super", true);
        new Bewertung(new Kunde("K-002", "Max Mustermann"), produkt, 4, "Gut", true);
        new Bewertung(new Kunde("K-003", "Max"), produkt, 2, "Naja", false);

        assertThat(produkt.berechneDurchschnittssterne()).isCloseTo(11.0 / 3, within(1e-9));
    }

    @Test
    void testDurchschnittGrenzwerte() {
        Produkt anderes = new Produkt("P-002", "Kabel", new BigDecimal("5"), hersteller);
        new Bewertung(kunde, produkt, 1, "Schlecht", true);
        new Bewertung(kunde, anderes, 5, "Top", true);

        assertThat(produkt.berechneDurchschnittssterne()).isEqualTo(1.0);
        assertThat(anderes.berechneDurchschnittssterne()).isEqualTo(5.0);
    }

    @Test
    void testDurchschnittVeraendertNichts() {
        new Bewertung(kunde, produkt, 3, "Okay", true);

        produkt.berechneDurchschnittssterne();

        assertThat(produkt.showBewertungen()).hasSize(1);
    }

    @Test
    void testBewertungHinzufuegenInvalid() {
        Produkt anderes = new Produkt("P-002", "Kabel", new BigDecimal("5"), hersteller);
        Bewertung fremde = new Bewertung(kunde, anderes, 3, "Okay", true);

        assertThatThrownBy(() -> produkt.bewertungHinzufuegen(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> produkt.bewertungHinzufuegen(fremde))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.showBewertungen()).isEmpty();
    }
}
