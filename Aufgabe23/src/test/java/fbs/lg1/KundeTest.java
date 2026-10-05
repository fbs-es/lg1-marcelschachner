package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KundeTest {

    @Test
    void testInit() {
        Kunde kunde = new Kunde(1, "Marcel Schachner", "marcel@example.com");

        assertThat(kunde.showKundennummer()).isEqualTo(1);
        assertThat(kunde.showName()).isEqualTo("Marcel Schachner");
        assertThat(kunde.showEmail()).isEqualTo("marcel@example.com");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Kunde(0, "Marcel", "marcel@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, null, "marcel@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, " ", "marcel@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, "Marcel", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, "Marcel", "marcel.example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
