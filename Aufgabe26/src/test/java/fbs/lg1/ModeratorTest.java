package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModeratorTest {

    private Moderator moderator;

    @BeforeEach
    void prepare() {
        moderator = new Moderator("Marcel Schachner");
    }

    @Test
    void testInit() {
        assertThat(moderator.showName()).isEqualTo("Marcel Schachner");
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Moderator(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Moderator(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
