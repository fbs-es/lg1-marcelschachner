package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KundeTest {

    @Test
    void testInit() {
        Kunde kunde = new Kunde("K-001", "Marcel Schachner");

        assertThat(kunde.showKundenNummer()).isEqualTo("K-001");
        assertThat(kunde.showName()).isEqualTo("Marcel Schachner");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Kunde(null, "Marcel"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(" ", "Marcel"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde("K-1", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde("K-1", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
