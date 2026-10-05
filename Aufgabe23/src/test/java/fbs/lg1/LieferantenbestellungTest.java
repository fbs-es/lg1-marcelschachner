package fbs.lg1;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LieferantenbestellungTest {

    private Lieferant lieferant;
    private Produkt produkt;
    private Date datum;

    @BeforeEach
    void prepare() {
        lieferant = new Lieferant(1, "Logitech");
        produkt = new Produkt(1, "Maus", 10.0, 100, 5, lieferant);
        datum = new Date();
    }

    @Test
    void testInit() {
        Lieferantenbestellung lieferantenbestellung =
                new Lieferantenbestellung(1, datum, 10, produkt, lieferant);

        assertThat(lieferantenbestellung.showLieferantenbestellnummer()).isEqualTo(1);
        assertThat(lieferantenbestellung.showDatum()).isSameAs(datum);
        assertThat(lieferantenbestellung.showMenge()).isEqualTo(10);
        assertThat(lieferantenbestellung.showProdukt()).isSameAs(produkt);
        assertThat(lieferantenbestellung.showLieferant()).isSameAs(lieferant);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Lieferantenbestellung(0, datum, 10, produkt, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Lieferantenbestellung(1, null, 10, produkt, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Lieferantenbestellung(1, datum, 0, produkt, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Lieferantenbestellung(1, datum, 10, null, lieferant))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Lieferantenbestellung(1, datum, 10, produkt, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
