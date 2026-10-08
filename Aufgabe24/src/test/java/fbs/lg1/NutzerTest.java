package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NutzerTest {

    @Test
    void testInit() {
        Nutzer nutzer = new Nutzer(1, "Marcel Schachner", "schachner.marcel@icloud.com");

        assertThat(nutzer.showId()).isEqualTo(1);
        assertThat(nutzer.showName()).isEqualTo("Marcel Schachner");
        assertThat(nutzer.showEmail()).isEqualTo("schachner.marcel@icloud.com");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Nutzer(0, "Marcel Schachner", "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Nutzer(-1, "Marcel Schachner", "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Nutzer(1, null, "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Nutzer(1, " ", "schachner.marcel@icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Nutzer(1, "Marcel Schachner", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Nutzer(1, "Marcel Schachner", "schachner.marcel.icloud.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
