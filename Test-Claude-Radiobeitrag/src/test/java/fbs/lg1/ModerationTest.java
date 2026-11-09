package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModerationTest {

    private Moderation moderation;

    @BeforeEach
    void prepare() {
        moderation = new Moderation("Begrüßung", 60, "Guten Morgen!");
    }

    @Test
    void testInit() {
        assertThat(moderation.showTitel()).isEqualTo("Begrüßung");
        assertThat(moderation.showDauer()).isEqualTo(60);
        assertThat(moderation.showSprechertext()).isEqualTo("Guten Morgen!");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Moderation("Begrüßung", 60, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Moderation("Begrüßung", 0, "Guten Morgen!"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testErstelleFortsetzung() {
        Radiobeitrag fortsetzung = moderation.erstelleFortsetzung(20);

        assertThat(fortsetzung).isInstanceOf(Moderation.class);
        assertThat(fortsetzung.showDauer()).isEqualTo(20);
        assertThat(((Moderation) fortsetzung).showSprechertext()).isEqualTo("Guten Morgen!");
    }
}
