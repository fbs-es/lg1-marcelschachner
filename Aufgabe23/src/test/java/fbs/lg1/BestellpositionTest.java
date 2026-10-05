package fbs.lg1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BestellpositionTest {

    private Produkt produkt;

    @BeforeEach
    void prepare() {
        Lieferant lieferant = new Lieferant(1, "Logitech");
        produkt = new Produkt(1, "Maus", 10.0, 100, 5, lieferant);
    }

    @Test
    void testInit() {
        Bestellposition position = new Bestellposition(1, 3, 10.0, produkt);

        assertThat(position.showPositionsnummer()).isEqualTo(1);
        assertThat(position.showMenge()).isEqualTo(3);
        assertThat(position.showEinzelpreis()).isEqualTo(10.0);
        assertThat(position.showProdukt()).isSameAs(produkt);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Bestellposition(0, 1, 10.0, produkt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellposition(1, 0, 10.0, produkt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellposition(1, 1, 0.0, produkt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellposition(-1, 1, 10.0, produkt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellposition(1, -1, 10.0, produkt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellposition(1, 1, -10.0, produkt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Bestellposition(1, 1, 10.0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBerechnePositionswert() {
        Bestellposition position = new Bestellposition(1, 3, 2.5, produkt);

        assertThat(position.berechnePositionswert()).isEqualTo(7.5);
    }
}
