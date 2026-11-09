package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WerbebeitragTest {

    private Werbebeitrag werbung;

    @BeforeEach
    void prepare() {
        werbung = new Werbebeitrag("Kaffee-Spot", 30, "Bohnenglück GmbH", 5000.0);
    }

    @Test
    void testInit() {
        assertThat(werbung.showTitel()).isEqualTo("Kaffee-Spot");
        assertThat(werbung.showDauer()).isEqualTo(30);
        assertThat(werbung.showKunde()).isEqualTo("Bohnenglück GmbH");
        assertThat(werbung.showWerbebudget()).isEqualTo(5000.0);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Werbebeitrag(" ", 30, "Bohnenglück GmbH", 5000.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Werbebeitrag("Kaffee-Spot", -1, "Bohnenglück GmbH", 5000.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Werbebeitrag("Kaffee-Spot", 30, null, 5000.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Werbebeitrag("Kaffee-Spot", 30, "Bohnenglück GmbH", -0.01))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testErstelleFortsetzung() {
        Radiobeitrag fortsetzung = werbung.erstelleFortsetzung(10);

        assertThat(fortsetzung).isInstanceOf(Werbebeitrag.class);
        assertThat(fortsetzung.showTitel()).isEqualTo("Kaffee-Spot (Fortsetzung)");
        assertThat(fortsetzung.showDauer()).isEqualTo(10);
        assertThat(((Werbebeitrag) fortsetzung).showKunde()).isEqualTo("Bohnenglück GmbH");
    }
}
