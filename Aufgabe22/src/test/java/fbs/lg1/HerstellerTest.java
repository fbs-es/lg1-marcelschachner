package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HerstellerTest {

    @Test
    void testInit() {
        Hersteller hersteller = new Hersteller("Logitech", "support@logitech.com");

        assertThat(hersteller.showName()).isEqualTo("Logitech");
        assertThat(hersteller.showSupportEmail()).isEqualTo("support@logitech.com");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Hersteller(null, "support@logitech.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Hersteller(" ", "support@logitech.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Hersteller("Logitech", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Hersteller("Logitech", "keine-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
