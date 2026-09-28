package fbs.lg1;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AusleihungTest {

    private FakeClock clock;
    private Scooter scooter;

    @BeforeEach
    void prepare() {
        clock = FakeClock.start();
        scooter = new Scooter("S-001");
    }

    @Test
    void testInit() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        assertThat(ausleihung.showAusleiheId()).startsWith("A-");
        assertThat(ausleihung.showScooter()).isSameAs(scooter);
        assertThat(ausleihung.showStartzeit()).isEqualTo(LocalDateTime.of(2026, 9, 28, 10, 0));
        assertThat(ausleihung.showEndzeit()).isNull();
        assertThat(ausleihung.showKosten()).isZero();
        assertThat(ausleihung.istBeendet()).isFalse();
        assertThat(scooter.checkIstGesperrt()).isFalse();
    }

    @Test
    void testUniqueIds() {
        Ausleihung first = new Ausleihung(scooter, clock);
        Ausleihung second = new Ausleihung(new Scooter("S-002"), clock);
        assertThat(first.showAusleiheId()).isNotEqualTo(second.showAusleiheId());
    }

    @Test
    void testInitInvalidArguments() {
        assertThatThrownBy(() -> new Ausleihung(null, clock)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ausleihung(scooter, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(scooter.checkIstGesperrt()).isTrue();
    }

    @Test
    void testInitScooterInUse() {
        new Ausleihung(scooter, clock);
        assertThatThrownBy(() -> new Ausleihung(scooter, clock)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testInitAkkuExactly15() {
        scooter.akkuVerringern(85);
        assertThatThrownBy(() -> new Ausleihung(scooter, clock)).isInstanceOf(IllegalStateException.class);
        assertThat(scooter.checkIstGesperrt()).isTrue();
    }

    @Test
    void testInitAkku16() {
        scooter.akkuVerringern(84);
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        assertThat(ausleihung.istBeendet()).isFalse();
        assertThat(scooter.checkIstGesperrt()).isFalse();
    }

    @Test
    void testBeenden() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofMinutes(10));
        ausleihung.beenden();
        assertThat(ausleihung.istBeendet()).isTrue();
        assertThat(ausleihung.showEndzeit()).isEqualTo(LocalDateTime.of(2026, 9, 28, 10, 10));
        assertThat(ausleihung.showKosten()).isEqualTo(2.00);
        assertThat(scooter.checkAkkustand()).isEqualTo(90);
        assertThat(scooter.checkIstGesperrt()).isTrue();
    }

    @Test
    void testBeendenCostsExact() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofMinutes(3));
        ausleihung.beenden();
        assertThat(ausleihung.showKosten()).isEqualTo(0.60);
    }

    @Test
    void testBeendenStartedMinuteCounts() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofSeconds(1));
        ausleihung.beenden();
        assertThat(ausleihung.showKosten()).isEqualTo(0.20);
        assertThat(scooter.checkAkkustand()).isEqualTo(99);
    }

    @Test
    void testBeendenPartialMinuteRoundsUp() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofMinutes(4).plusSeconds(30));
        ausleihung.beenden();
        assertThat(ausleihung.showKosten()).isEqualTo(1.00);
        assertThat(scooter.checkAkkustand()).isEqualTo(95);
    }

    @Test
    void testBeendenLongRideAkkuNotBelowZero() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofMinutes(150));
        ausleihung.beenden();
        assertThat(ausleihung.showKosten()).isEqualTo(30.00);
        assertThat(scooter.checkAkkustand()).isZero();
    }

    @Test
    void testBeendenZeroDuration() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        assertThatThrownBy(ausleihung::beenden).isInstanceOf(IllegalStateException.class);
        assertThat(ausleihung.istBeendet()).isFalse();
        assertThat(ausleihung.showKosten()).isZero();
        assertThat(scooter.checkIstGesperrt()).isFalse();
        assertThat(scooter.checkAkkustand()).isEqualTo(100);
    }

    @Test
    void testBeendenClockBackwards() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofMinutes(-5));
        assertThatThrownBy(ausleihung::beenden).isInstanceOf(IllegalStateException.class);
        assertThat(ausleihung.istBeendet()).isFalse();
    }

    @Test
    void testBeendenTwice() {
        Ausleihung ausleihung = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofMinutes(5));
        ausleihung.beenden();
        clock.advance(Duration.ofMinutes(5));
        assertThatThrownBy(ausleihung::beenden).isInstanceOf(IllegalStateException.class);
        assertThat(ausleihung.showKosten()).isEqualTo(1.00);
        assertThat(ausleihung.showEndzeit()).isEqualTo(LocalDateTime.of(2026, 9, 28, 10, 5));
        assertThat(scooter.checkAkkustand()).isEqualTo(95);
    }

    @Test
    void testScooterReusableAfterBeenden() {
        Ausleihung first = new Ausleihung(scooter, clock);
        clock.advance(Duration.ofMinutes(5));
        first.beenden();
        Ausleihung second = new Ausleihung(scooter, clock);
        assertThat(second.istBeendet()).isFalse();
        assertThat(scooter.checkIstGesperrt()).isFalse();
    }
}
