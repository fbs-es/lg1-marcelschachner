package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

class KaufTest {

    private Kunde kunde;
    private Produkt produkt;

    @BeforeEach
    void prepare() {
        Hersteller hersteller = new Hersteller("Logitech", "support@logitech.com");
        produkt = new Produkt("P-001", "Maus", new BigDecimal("19.99"), hersteller);
        kunde = new Kunde("K-001", "Marcel Schachner");
    }

    @Test
    void testInit() {
        LocalDate kaufDatum = LocalDate.now().minusDays(3);

        Kauf kauf = new Kauf(kunde, produkt, kaufDatum);

        assertThat(kauf.showKunde()).isSameAs(kunde);
        assertThat(kauf.showProdukt()).isSameAs(produkt);
        assertThat(kauf.showKaufDatum()).isEqualTo(kaufDatum);
    }

    @Test
    void testInitHeute() {
        Kauf kauf = new Kauf(kunde, produkt, LocalDate.now());

        assertThat(kauf.showKaufDatum()).isEqualTo(LocalDate.now());
    }

    @Test
    void testInitInvalid() {
        LocalDate heute = LocalDate.now();

        assertThatThrownBy(() -> new Kauf(null, produkt, heute))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kauf(kunde, null, heute))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kauf(kunde, produkt, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInitZukunft() {
        LocalDate morgen = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> new Kauf(kunde, produkt, morgen))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
