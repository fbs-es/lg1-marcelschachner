package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KundeTest {

    @Test
    void testInit() {
        Kunde kunde = new Kunde(1, "Marcel Schachner", "schachner.marcel@icloud.com");

        assertThat(kunde.showKundennummer()).isEqualTo(1);
        assertThat(kunde.showName()).isEqualTo("Marcel Schachner");
        assertThat(kunde.showEmail()).isEqualTo("schachner.marcel@icloud.com");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Kunde(0, "Marcel Schachner", "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, null, "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, " ", "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(-1, "Marcel Schachner", "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, "", "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, "Marcel Schachner", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(1, "Marcel Schachner", "schachner.marcel.icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
