package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LieferantTest {

    @Test
    void testInit() {
        Lieferant lieferant = new Lieferant(1, "Logitech");

        assertThat(lieferant.showLieferantennummer()).isEqualTo(1);
        assertThat(lieferant.showName()).isEqualTo("Logitech");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Lieferant(0, "Logitech"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Lieferant(1, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Lieferant(1, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
