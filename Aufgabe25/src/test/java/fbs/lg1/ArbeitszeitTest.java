package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArbeitszeitTest {

    private Arbeitszeit arbeitszeit;

    @BeforeEach
    void prepare() {
        arbeitszeit = new Arbeitszeit(LocalDate.of(2026, 10, 1), 90);
    }

    @Test
    void testInit() {
        assertThat(arbeitszeit.showDatum()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(arbeitszeit.showMinuten()).isEqualTo(90);
        assertThat(arbeitszeit.istAbgerechnet()).isFalse();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Arbeitszeit(null, 90))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Arbeitszeit(LocalDate.of(2026, 10, 1), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAbrechnen() {
        arbeitszeit.abrechnen();

        assertThat(arbeitszeit.istAbgerechnet()).isTrue();
    }

    @Test
    void testAbrechnenBereitsAbgerechnet() {
        arbeitszeit.abrechnen();

        assertThatThrownBy(() -> arbeitszeit.abrechnen()).isInstanceOf(IllegalStateException.class);
    }
}
