package fbs.lg1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScooterTest {

    @Test
    void testInit() {
        Scooter scooter = new Scooter("S-001");
        assertThat(scooter.showScooterId()).isEqualTo("S-001");
        assertThat(scooter.checkAkkustand()).isEqualTo(100);
        assertThat(scooter.checkIstGesperrt()).isTrue();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Scooter(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Scooter("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Scooter("   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEntsperren() {
        Scooter scooter = new Scooter("S-001");
        scooter.entsperren();
        assertThat(scooter.checkIstGesperrt()).isFalse();
    }

    @Test
    void testEntsperrenTwice() {
        Scooter scooter = new Scooter("S-001");
        scooter.entsperren();
        assertThatThrownBy(scooter::entsperren).isInstanceOf(IllegalStateException.class);
        assertThat(scooter.checkIstGesperrt()).isFalse();
    }

    @Test
    void testSperren() {
        Scooter scooter = new Scooter("S-001");
        scooter.entsperren();
        scooter.sperren();
        assertThat(scooter.checkIstGesperrt()).isTrue();
    }

    @Test
    void testSperrenAlreadyLocked() {
        Scooter scooter = new Scooter("S-001");
        assertThatThrownBy(scooter::sperren).isInstanceOf(IllegalStateException.class);
        assertThat(scooter.checkIstGesperrt()).isTrue();
    }

    @Test
    void testAkkuVerringern() {
        Scooter scooter = new Scooter("S-001");
        scooter.akkuVerringern(30);
        assertThat(scooter.checkAkkustand()).isEqualTo(70);
    }

    @Test
    void testAkkuVerringernExactlyToZero() {
        Scooter scooter = new Scooter("S-001");
        scooter.akkuVerringern(100);
        assertThat(scooter.checkAkkustand()).isZero();
    }

    @Test
    void testAkkuVerringernNeverBelowZero() {
        Scooter scooter = new Scooter("S-001");
        scooter.akkuVerringern(150);
        assertThat(scooter.checkAkkustand()).isZero();
        scooter.akkuVerringern(1);
        assertThat(scooter.checkAkkustand()).isZero();
    }

    @Test
    void testAkkuVerringernInvalid() {
        Scooter scooter = new Scooter("S-001");
        assertThatThrownBy(() -> scooter.akkuVerringern(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> scooter.akkuVerringern(-5)).isInstanceOf(IllegalArgumentException.class);
        assertThat(scooter.checkAkkustand()).isEqualTo(100);
    }
}
