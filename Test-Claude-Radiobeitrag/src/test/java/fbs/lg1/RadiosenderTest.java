package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RadiosenderTest {

    private Radioprogramm programm;
    private Radiosender sender;

    @BeforeEach
    void prepare() {
        programm = new Radioprogramm(LocalDate.of(2026, 11, 9));
        sender = new Radiosender("Cloud-FM", 101.7, programm);
    }

    @Test
    void testInit() {
        assertThat(sender.showName()).isEqualTo("Cloud-FM");
        assertThat(sender.showFrequenz()).isEqualTo(101.7);
        assertThat(sender.showRadioprogramm()).isSameAs(programm);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Radiosender(" ", 101.7, programm))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosender("Cloud-FM", 0.0, programm))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Radiosender("Cloud-FM", 101.7, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
